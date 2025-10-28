package dogapi;

import java.util.*;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {
    // TODO Task 2: Complete this class
    private final BreedFetcher delegate;
    private final Map<String, List<String>> cache = new HashMap<>();
    private int callsMade = 0;
    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.delegate = Objects.requireNonNull(fetcher);
    }

    @Override
    public List<String> getSubBreeds(String breed) {
        if (breed == null || breed.trim().isEmpty()) {
            throw new BreedNotFoundException("breed");
        }

        String key = normalized(breed);
        List<String> result = cache.get(key);
        if (result != null) {
            return result;
        }

        callsMade++;
        List<String> results = delegate.getSubBreeds(breed);
        List<String> immutable = Collections.unmodifiableList(new ArrayList<>(results));
        cache.put(key, immutable);
        return immutable;
    }

    public int getCallsMade() { return callsMade;}

    private String normalized(String breed) {
        if (breed == null) {
            return breed;
        }
        return breed.trim().toLowerCase(Locale.ROOT);
    }
}