package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSacrificedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a permanent copy of the permanent sacrificed to pay an activated ability's cost. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfSacrificedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfSacrificedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent sacrificed = entry.getSacrificedPermanentSnapshot();
        if (source == null || sacrificed == null || sacrificed.getCard() == null) {
            return;
        }

        // "except it has this ability" is part of the copiable values once the source has copied something
        // (a Clone of the Dane keeps it), so the retained ability is read from the source's current card.
        boolean copiedAbilityAlreadyPresent = sacrificed.getCard().getActivatedAbilities().stream()
                .anyMatch(BecomeCopyOfSacrificedPermanentEffectHandler::isBecomeCopyAbility);
        List<ActivatedAbility> retainedAbilities = copiedAbilityAlreadyPresent
                ? List.of()
                : source.getCard().getActivatedAbilities().stream()
                        .filter(BecomeCopyOfSacrificedPermanentEffectHandler::isBecomeCopyAbility)
                        .toList();
        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(
                source, sacrificed.getCard(), null, null, java.util.Set.of(), retainedAbilities);
        gameLogService.append(gameData,
                GameLog.text(originalName + " becomes a copy of " + sacrificed.getCard().getName() + "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName,
                sacrificed.getCard().getName());
    }

    private static boolean isBecomeCopyAbility(ActivatedAbility ability) {
        return ability.getEffects().stream()
                .anyMatch(candidate -> candidate instanceof BecomeCopyOfSacrificedPermanentEffect);
    }
}
