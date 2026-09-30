package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesOfChosenManaValueParityCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Applies the temporary global blocking restriction after an odd/even choice resolves. */
@Component
@RequiredArgsConstructor
public class CreaturesOfChosenManaValueParityCantBlockThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreaturesOfChosenManaValueParityCantBlockThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ManaValueParity chosen = gameData.chosenSpellManaValueParity;
        if (chosen == null) {
            return;
        }
        gameData.chosenSpellManaValueParity = null;

        var creatureParity = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentManaValueParityPredicate(chosen)));
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard().getName(),
                entry.getSourcePermanentId(),
                entry.getControllerId(),
                new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                        creatureParity,
                        new PermanentTruePredicate(),
                        "Creatures with " + chosen.name().toLowerCase() + " mana values can't block this turn"),
                null,
                null,
                creatureParity,
                EffectDuration.UNTIL_END_OF_TURN,
                0));
        gameLogService.append(gameData, GameLog.text(
                "Creatures with " + chosen.name().toLowerCase() + " mana values can't block this turn."));
    }
}
