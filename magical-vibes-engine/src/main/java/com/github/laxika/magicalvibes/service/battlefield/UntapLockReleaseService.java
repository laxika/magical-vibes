package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Ends what lasts "for as long as [a permanent] remains tapped" when that permanent untaps or leaves the battlefield
 * (Giant Oyster): the {@code WHILE_SOURCE_TAPPED} untap locks it holds are released. The delayed triggered abilities
 * of the same duration end as their trigger event occurs ({@code SourceRemainsTapped}).
 *
 * <p>Called from the two events that end such a duration: the untap paths
 * ({@code TapUntapSupport.untapPermanent}, {@code MayMiscHandlerService.handleMayNotUntapChoice}) and the single
 * battlefield-removal cleanup point in {@link PermanentRemovalService}. A permanent nothing watches is untouched, so
 * it is cheap to call unconditionally.
 */
@Slf4j
@Component
public class UntapLockReleaseService {

    /** Releases every untap lock {@code source} holds. */
    public void releaseUntapLocks(GameData gameData, Permanent source) {
        UUID sourceId = source.getId();
        gameData.forEachPermanent((playerId, p) -> p.getUntapPreventedByPermanentIds().remove(sourceId));
    }
}
