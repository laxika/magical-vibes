package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardThenBottomRestEffect;
import com.github.laxika.magicalvibes.model.effect.PossibilityStormExileAndCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Possibility Storm's triggered ability: exile the triggering spell, exile from the top of
 * the caster's library until a card sharing a card type with it turns up, offer that card as a free
 * cast, then bottom everything the enchantment exiled in a random order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PossibilityStormExileAndCastEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final ExileBottomRandomSupport exileBottomRandomSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PossibilityStormExileAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (PossibilityStormExileAndCastEffect) effect;
        UUID sourcePermanentId = typedEffect.sourcePermanentId();
        UUID castingPlayerId = typedEffect.castingPlayerId();
        String playerName = gameData.playerIdToName.get(castingPlayerId);

        StackEntry originalSpell = null;
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getCard().getId().equals(typedEffect.originalSpellCardId())) {
                originalSpell = stackEntry;
                break;
            }
        }

        if (originalSpell == null) {
            // The spell already left the stack (countered, or exiled by another Possibility Storm).
            log.info("Game {} - Possibility Storm's trigger finds no spell on the stack", gameData.id);
            return;
        }

        Card originalCard = originalSpell.getCard();
        gameData.stack.remove(originalSpell);
        exileService.exileCard(gameData, castingPlayerId, originalCard, sourcePermanentId);
        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " exiles ", originalCard, " (Possibility Storm)."));

        EnumSet<CardType> spellTypes = EnumSet.of(originalCard.getType());
        spellTypes.addAll(originalCard.getAdditionalTypes());

        Card match = exileUntilSharedType(gameData, castingPlayerId, sourcePermanentId, spellTypes);

        if (match == null) {
            gameLogService.append(gameData, GameLog.text(playerName
                    + " exiles their entire library — no card shares a card type with the exiled spell."));
            exileBottomRandomSupport.bottomCardsExiledWithSource(gameData, sourcePermanentId, null);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                match,
                castingPlayerId,
                List.of(new MayCastExiledCardThenBottomRestEffect(sourcePermanentId)),
                "Cast " + match.getName() + " without paying its mana cost?",
                match.getId()
        ));
    }

    /**
     * Exiles cards from the top of the player's library, one at a time, until one shares a card type
     * with the exiled spell. Returns that card, or {@code null} if the library ran out first. Every
     * card exiled this way is tracked with the enchantment so the bottoming clause sees it.
     */
    private Card exileUntilSharedType(GameData gameData, UUID playerId, UUID sourcePermanentId,
            EnumSet<CardType> spellTypes) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null) {
            return null;
        }

        List<Card> exiled = new ArrayList<>();
        Card match = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            exiled.add(card);
            exileService.exileCard(gameData, playerId, card, sourcePermanentId);
            if (sharesType(card, spellTypes)) {
                match = card;
                break;
            }
        }

        if (!exiled.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                    + " exiles " + exiled.size() + " card" + (exiled.size() == 1 ? "" : "s")
                    + " from the top of their library (Possibility Storm)."));
        }
        return match;
    }

    private boolean sharesType(Card card, EnumSet<CardType> spellTypes) {
        if (spellTypes.contains(card.getType())) {
            return true;
        }
        for (CardType additionalType : card.getAdditionalTypes()) {
            if (spellTypes.contains(additionalType)) {
                return true;
            }
        }
        return false;
    }
}
