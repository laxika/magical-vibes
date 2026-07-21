package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessReturnLandToHandEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Discard a card unless you return a land you control to its owner's hand." (Tragic Lesson)
 *
 * <p>When the controller has a land to return, this asks via a "you may" ability whose accept
 * branch returns a chosen land and whose decline branch discards a card. With no land the discard
 * is mandatory — the same shape as {@link DiscardUnlessExileCardFromGraveyardEffectHandler}.
 */
@Component
@RequiredArgsConstructor
public class DiscardUnlessReturnLandToHandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardUnlessReturnLandToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardUnlessReturnLandToHandEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        boolean hasLand = battlefield != null && battlefield.stream()
                .anyMatch(p -> p.getCard().hasType(CardType.LAND));

        if (!hasLand) {
            // No land to return — the discard is mandatory.
            gameData.discardCausedByOpponent = false;
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 1);
            return;
        }

        String prompt = "Return a land you control to its owner's hand to avoid discarding? ("
                + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(e), prompt
        ));
    }
}
