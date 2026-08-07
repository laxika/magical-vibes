package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.MayCastRevealedSpellWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared machinery for "reveal the top N cards of a library, cast up to K instants/sorceries from
 * among them for free, then the rest go into the graveyard" (Talent of the Telepath).
 *
 * <p>The revealed cards are held outside every zone in a
 * {@link PendingInteraction.RevealedFreeCastGroup} while the casting decisions run, so a card that
 * is cast never passes through the graveyard. Each round offers one {@link PendingMayAbility} per
 * still-held instant or sorcery; when no cast may follow — the limit is spent, nothing castable is
 * left, or every offer was declined — the held cards are put into the revealing player's graveyard.
 */
@Component
@RequiredArgsConstructor
public class RevealedFreeCastSupport {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    /**
     * Offers the next round of free-cast choices for the held cards, or dumps them into the
     * revealing player's graveyard when no further cast is possible.
     */
    public void offerOrDump(GameData gameData, UUID ownerId, UUID casterId, List<Card> heldCards,
                            int castsRemaining) {
        List<Card> castable = heldCards.stream()
                .filter(c -> c.hasType(CardType.INSTANT) || c.hasType(CardType.SORCERY))
                .toList();

        if (castsRemaining <= 0 || castable.isEmpty()) {
            dumpToGraveyard(gameData, ownerId, heldCards);
            return;
        }

        gameData.queueInteraction(new PendingInteraction.RevealedFreeCastGroup(
                ownerId, casterId, new ArrayList<>(heldCards), castsRemaining));

        for (int i = castable.size() - 1; i >= 0; i--) {
            Card card = castable.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card, casterId,
                    List.of(new MayCastRevealedSpellWithoutPayingManaCostEffect()),
                    "Cast " + card.getName() + " without paying its mana cost?"));
        }
    }

    /** Puts every still-held revealed card into the revealing player's graveyard. */
    public void dumpToGraveyard(GameData gameData, UUID ownerId, List<Card> heldCards) {
        if (heldCards.isEmpty()) return;

        GameLog.Builder log = GameLog.builder()
                .text(gameData.playerIdToName.get(ownerId) + " puts ");
        for (int i = 0; i < heldCards.size(); i++) {
            if (i > 0) log.text(", ");
            log.card(heldCards.get(i));
        }
        log.text(" into their graveyard.");

        for (Card card : new ArrayList<>(heldCards)) {
            graveyardService.addCardToGraveyard(gameData, ownerId, card);
        }
        gameLogService.append(gameData, log.build());
    }

    /** True while at least one free-cast offer from the current group is still queued. */
    public boolean hasPendingOffers(GameData gameData) {
        return gameData.pendingMayAbilities.stream()
                .anyMatch(pma -> pma.effects().stream()
                        .anyMatch(e -> e instanceof MayCastRevealedSpellWithoutPayingManaCostEffect));
    }

    /** Drops every queued free-cast offer (used when one is accepted). */
    public void clearPendingOffers(GameData gameData) {
        gameData.pendingMayAbilities.removeIf(pma -> pma.effects().stream()
                .anyMatch(e -> e instanceof MayCastRevealedSpellWithoutPayingManaCostEffect));
    }
}
