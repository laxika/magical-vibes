package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared tap/untap helpers used by every "normal" Tap/Untap effect handler.
 *
 * <p>Extracted verbatim from the original {@code TapUntapResolutionService} monolith;
 * behavior (trigger order) is identical.
 */
@Component
@RequiredArgsConstructor
public class TapUntapSupport {

    private final TriggerCollectionService triggerCollectionService;

    /**
     * Taps the permanent and fires enchanted-permanent tap triggers if it was not already tapped.
     *
     * @return true if the permanent was newly tapped (was untapped before)
     */
    public boolean tapPermanent(GameData gameData, Permanent permanent) {
        boolean wasTapped = permanent.isTapped();
        permanent.tap();
        if (!wasTapped) {
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, permanent);
            return true;
        }
        return false;
    }
}
