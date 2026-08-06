package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Makes the source permanent a copy of the target land, then re-grants the activated ability that
 * produced this effect so the copy keeps "except it has this ability" (Thespian's Stage).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null || entry.getTargetId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            log.info("Game {} - Become-copy-of-land source or target no longer on the battlefield", gameData.id);
            return;
        }

        // Snapshot the granting ability before the copy replaces the source's card.
        List<ActivatedAbility> retained = new ArrayList<>();
        for (ActivatedAbility ability : source.getCard().getActivatedAbilities()) {
            if (ability.getEffects().stream().anyMatch(e -> e instanceof BecomeCopyOfTargetLandEffect)) {
                retained.add(ability);
            }
        }

        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, target, null, null);

        Card copiedCard = source.getCard();
        for (ActivatedAbility ability : retained) {
            copiedCard.addActivatedAbility(ability);
        }

        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, target.getCard().getName());
    }
}
