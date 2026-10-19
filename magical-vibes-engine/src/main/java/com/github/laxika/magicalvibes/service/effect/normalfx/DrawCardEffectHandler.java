package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class DrawCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final DrawService drawService;

    public DrawCardEffectHandler(PlayerInteractionSupport playerInteractionSupport,
                                 GameQueryService gameQueryService,
                                 AmountEvaluationService amountEvaluationService,
                                 @Lazy DrawService drawService) {
        this.playerInteractionSupport = playerInteractionSupport;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
        this.drawService = drawService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawCardEffect) effect;

        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        if (e.rememberAmount()) entry.setEventValue(amount);

        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        Set<UUID> cardsInHandBeforeDraw = new HashSet<>();
        hand.forEach(card -> cardsInHandBeforeDraw.add(card.getId()));
        Set<UUID> cardsInLibraryBeforeDraw = new HashSet<>();
        gameData.playerDecks.getOrDefault(entry.getControllerId(), List.of())
                .forEach(card -> cardsInLibraryBeforeDraw.add(card.getId()));

        if (entry.isCyclingAbility()) {
            for (int i = 0; i < amount; i++) {
                drawService.resolveCyclingDrawCard(gameData, entry.getControllerId(), entry.getCard());
                if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                    break;
                }
            }
        } else {
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), amount);
        }

        // Only cards that came out of the library are drawn; a permanent returned to hand by a replacement
        // (Words of Wind) is a new hand card but was not drawn.
        List<Card> newlyDrawn = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(card -> !cardsInHandBeforeDraw.contains(card.getId()))
                .filter(card -> cardsInLibraryBeforeDraw.contains(card.getId()))
                .toList();
        // More cards than instructed means a draw was replaced by drawing extra cards (e.g. Alhammarret's
        // Archive); those cards were not drawn by this effect's instruction, so "that card" does not exist.
        if (newlyDrawn.size() <= amount) {
            newlyDrawn.forEach(card -> entry.recordCardDrawnThisResolution(card.getId()));
        }
    }
}
