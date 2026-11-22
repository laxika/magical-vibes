package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link DrawCardEffect}: the players its subject names (through {@link SubjectResolutionService}) each draw
 * the amount, evaluated once, in APNAP order. The cards each player actually drew from the library are recorded as
 * the step's result.
 */
@Component
public class DrawCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;
    private final DrawService drawService;

    public DrawCardEffectHandler(PlayerInteractionSupport playerInteractionSupport,
                                 GameQueryService gameQueryService,
                                 AmountEvaluationService amountEvaluationService,
                                 SubjectResolutionService subjectResolutionService,
                                 @Lazy DrawService drawService) {
        this.playerInteractionSupport = playerInteractionSupport;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
        this.subjectResolutionService = subjectResolutionService;
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

        List<UUID> drawnCardIds = new ArrayList<>();
        List<UUID> drawingPlayerIds = new ArrayList<>();
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, e.player())) {
            if (gameData.playerIds.contains(playerId)) {
                List<UUID> drawn = draw(gameData, entry, playerId, amount);
                drawnCardIds.addAll(drawn);
                if (!drawn.isEmpty()) {
                    drawingPlayerIds.add(playerId);
                }
            }
        }
        entry.recordStep(StepKind.DRAW, StepOutcome.ofObjects(drawnCardIds, drawingPlayerIds));
    }

    /** {@code playerId} draws {@code amount} cards; returns the cards this instruction drew. */
    private List<UUID> draw(GameData gameData, StackEntry entry, UUID playerId, int amount) {
        DrawnThisWay snapshot = DrawnThisWay.before(gameData, playerId);

        if (entry.isCyclingAbility() && playerId.equals(entry.getControllerId())) {
            for (int i = 0; i < amount; i++) {
                drawService.resolveCyclingDrawCard(gameData, playerId, entry.getCard());
                if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                    break;
                }
            }
        } else {
            playerInteractionSupport.applyDrawCards(gameData, playerId, amount);
        }

        List<Card> newlyDrawn = snapshot.cards(gameData);
        // More cards than instructed means a draw was replaced by drawing extra cards (e.g. Alhammarret's
        // Archive); those cards were not drawn by this effect's instruction, so "that card" does not exist.
        if (newlyDrawn.size() > amount) {
            return List.of();
        }
        newlyDrawn.forEach(card -> entry.recordCardDrawnThisResolution(card.getId()));
        return newlyDrawn.stream().map(Card::getId).toList();
    }
}
