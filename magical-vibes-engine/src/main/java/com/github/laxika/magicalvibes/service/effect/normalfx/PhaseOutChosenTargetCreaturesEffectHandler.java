package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutChosenTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutUpToNControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the optional phase-out of any subset of a spell's surviving target creatures. */
@Component
@RequiredArgsConstructor
public class PhaseOutChosenTargetCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PhaseOutUpToNControlledPermanentsEffectHandler phaseOutHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutChosenTargetCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect).stream()
                .filter(id -> gameQueryService.findPermanentById(gameData, id) != null)
                .toList();
        if (targetIds.isEmpty()) {
            return;
        }

        PhaseOutUpToNControlledPermanentsEffect delegatedEffect =
                new PhaseOutUpToNControlledPermanentsEffect(
                        new Fixed(targetIds.size()),
                        new PermanentAnyOfPredicate(targetIds.stream()
                                .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new)
                                .toList()));
        phaseOutHandler.resolve(gameData, entry, delegatedEffect);
    }
}
