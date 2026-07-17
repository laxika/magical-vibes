package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
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
    private final CreatureControlService creatureControlService;

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

    /**
     * Untaps the permanent and fires "becomes untapped" triggers if it was tapped before.
     *
     * @return true if the permanent was newly untapped (was tapped before)
     */
    public boolean untapPermanent(GameData gameData, Permanent permanent) {
        boolean wasTapped = permanent.isTapped();
        permanent.untap();
        if (wasTapped) {
            // A "for as long as this stays tapped" control effect (Seasinger) ends here.
            creatureControlService.onSourceUntapped(gameData, permanent);
            triggerCollectionService.checkBecomesUntappedTriggers(gameData, permanent);
            return true;
        }
        return false;
    }
}
