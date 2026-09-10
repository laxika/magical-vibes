package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CastingPlayerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.DrawCardForTargetPlayerEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves Minamo's accepted draw for the player who cast the triggering spell. */
@Component
@RequiredArgsConstructor
public class CastingPlayerMayDrawCardHandler implements MayEffectHandlerBean {

    private final DrawCardForTargetPlayerEffectHandler drawHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastingPlayerMayDrawCardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            var drawEffect = new DrawCardForTargetPlayerEffect(1);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    player.getId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(drawEffect)),
                    player.getId(),
                    ability.sourcePermanentId());
            entry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            drawHandler.resolve(gameData, entry, drawEffect);
        }

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
