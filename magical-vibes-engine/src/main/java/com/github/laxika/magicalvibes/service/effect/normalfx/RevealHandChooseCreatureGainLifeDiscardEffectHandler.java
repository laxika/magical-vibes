package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandChooseCreatureGainLifeDiscardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Talara's Bane: the target opponent reveals their hand, the caster chooses one creature card whose
 * colors include any of the effect's colors, gains life equal to that card's toughness, then the
 * target discards it. The gain-life + discard is carried by
 * {@link PendingInteraction.RevealedHandChoice} (discard mode with
 * {@code gainLifeToChooserEqualToChosenToughness}) and applied by
 * {@link com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService#handleRevealedHandCardChosen}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealHandChooseCreatureGainLifeDiscardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandChooseCreatureGainLifeDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealHandChooseCreatureGainLifeDiscardEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        UUID casterId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);

        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, targetName + " reveals their hand. It is empty.");
            log.info("Game {} - {}'s hand is empty for Talara's Bane", gameData.id, targetName);
            return;
        }

        String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
        gameBroadcastService.logAndBroadcast(gameData, targetName + " reveals their hand: " + cardNames + ".");

        List<CardColor> colors = e.colors();
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }
            if (!colors.isEmpty() && card.getColors().stream().noneMatch(colors::contains)) {
                continue;
            }
            validIndices.add(i);
        }

        if (validIndices.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    casterName + " cannot choose a card (" + targetName + "'s hand has no matching creature card).");
            log.info("Game {} - {}'s hand has no matching creature for {}", gameData.id, targetName, casterName);
            return;
        }

        // A discard forced by an opponent enables replacement effects (e.g. Obstinate Baloth).
        gameData.discardCausedByOpponent = true;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                casterId, targetPlayerId, validIndices, 1, true, false,
                new ArrayList<>(), null, "Choose a creature card. You gain life equal to its toughness, then "
                + targetName + " discards it.", false, false, true));

        log.info("Game {} - {} choosing a creature from {}'s hand (gain life = toughness, then discard)",
                gameData.id, casterName, targetName);
    }
}
