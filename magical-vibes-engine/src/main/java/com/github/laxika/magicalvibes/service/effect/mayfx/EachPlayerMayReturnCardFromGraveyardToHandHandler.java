package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnCardsFromControllerGraveyardToHandEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Applies an accepted Minamo graveyard-return choice and advances the queued player choices. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayReturnCardFromGraveyardToHandHandler implements MayEffectHandlerBean {

    private final ReturnCardsFromControllerGraveyardToHandEffectHandler returnCardsHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayReturnCardFromGraveyardToHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            var mayEffect = (EachPlayerMayReturnCardFromGraveyardToHandEffect) ability.effects().getFirst();
            var returnEffect = new ReturnCardsFromControllerGraveyardToHandEffect(
                    mayEffect.filter(), new Fixed(1), false);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    player.getId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(returnEffect)),
                    0,
                    ability.sourcePermanentId());
            entry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            returnCardsHandler.resolve(gameData, entry, returnEffect);
        }

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
