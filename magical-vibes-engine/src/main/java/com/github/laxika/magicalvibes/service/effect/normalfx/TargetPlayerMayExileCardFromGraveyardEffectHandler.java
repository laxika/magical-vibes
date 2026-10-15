package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerMayExileCardFromGraveyardEffect;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Gravestorm: "target opponent may exile a card from their graveyard. If that player doesn't, you
 * may draw a card." Both choices are made during this resolution (CR 603.5), so the handler inserts
 * them as the next effects of the same stack entry. A player with an empty graveyard can't exile a
 * card, so the controller is offered the draw directly.
 */
@Component
public class TargetPlayerMayExileCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerMayExileCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getTargetId());
        MayEffect mayDraw = new MayEffect(new DrawCardEffect(), "Draw a card?");
        CardEffect next = graveyard == null || graveyard.isEmpty()
                ? mayDraw
                : new MayEffect(new TargetPlayerExilesCardFromGraveyardEffect(0),
                        "Exile a card from your graveyard?", mayDraw, MayChoicePlayer.TARGET_PLAYER);

        int index = entry.getEffectsToResolve().indexOf(effect);
        entry.insertEffectsToResolve(index < 0 ? entry.getEffectsToResolve().size() : index + 1, List.of(next));
    }
}
