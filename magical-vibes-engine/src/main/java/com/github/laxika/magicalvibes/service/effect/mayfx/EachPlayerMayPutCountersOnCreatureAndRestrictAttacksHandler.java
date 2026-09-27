package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Handles one player's optional Orzhov Advokist counter placement. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayPutCountersOnCreatureAndRestrictAttacksHandler implements MayEffectHandlerBean {

    private final EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect effect = effectHandler.effect(ability);
        if (accepted) {
            List<java.util.UUID> creatureIds = effectHandler.creatureIds(gameData, player.getId());
            if (creatureIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.OrzhovAdvokistCreatureChoice(
                                ability, effect));
                playerInputService.beginPermanentChoice(gameData, player.getId(), creatureIds,
                        ability.sourceCard().getName() + " - Choose a creature to put counters on.");
                return;
            }
            if (creatureIds.size() == 1) {
                effectHandler.accept(gameData, ability, creatureIds.getFirst());
            }
        }

        effectHandler.advance(gameData, ability.sourceCard(), effect, ability.sourcePermanentId());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
