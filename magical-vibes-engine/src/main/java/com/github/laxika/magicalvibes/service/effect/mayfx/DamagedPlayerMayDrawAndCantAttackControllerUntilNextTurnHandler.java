package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the damaged player's optional draw and its conditional attack restriction. */
@Component
@RequiredArgsConstructor
public class DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnHandler
        implements MayEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect)
                ability.effects().getFirst();
        if (accepted) {
            playerInteractionSupport.applyDrawCards(gameData, effect.damagedPlayerId(), 1);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    ability.sourceCard().getName(),
                    ability.sourcePermanentId(),
                    effect.protectedPlayerId(),
                    new CreaturesCantAttackControllerUnlessPredicateEffect(
                            new PermanentNotPredicate(new PermanentTruePredicate()), true,
                            effect.damagedPlayerId()),
                    null,
                    effect.protectedPlayerId(),
                    null,
                    EffectDuration.UNTIL_YOUR_NEXT_TURN,
                    0));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
