package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingReverseMiracleSearch;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ReverseMiracleCast;
import com.github.laxika.magicalvibes.model.effect.MayCastForReverseMiracleCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReverseMiracleSupport {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Autowired
    @Lazy
    private SpellCastingService spellCastingService;

    /** Offers the bottom card before the search changes the library, if it has reverse miracle. */
    public boolean offerBeforeSearch(GameData gameData, UUID playerId, LibrarySearchParams params,
                                     String prompt, boolean canFailToFind, String logMessage) {
        if (gameData.pendingReverseMiracleSearch != null
                || params.sourceSideboard()
                || params.sourceCards() != null
                || (params.targetPlayerId() != null && !params.targetPlayerId().equals(playerId))) {
            return false;
        }

        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            return false;
        }

        Card bottomCard = deck.getLast();
        ReverseMiracleCast reverseMiracle = bottomCard.getCastingOption(ReverseMiracleCast.class).orElse(null);
        if (reverseMiracle == null || reverseMiracle.manaCostString() == null) {
            return false;
        }

        gameData.pendingReverseMiracleSearch = new PendingReverseMiracleSearch(params, prompt, canFailToFind);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                bottomCard,
                playerId,
                List.of(new MayCastForReverseMiracleCostEffect()),
                "Cast " + bottomCard.getName() + " for its reverse miracle cost ("
                        + reverseMiracle.manaCostString() + ")?",
                null,
                reverseMiracle.manaCostString()
        ));
        gameLogService.append(gameData, GameLog.text(logMessage));
        log.info("Game {} - offering reverse miracle cast of {} for {}",
                gameData.id, bottomCard.getName(), reverseMiracle.manaCostString());
        return true;
    }

    public void handleChoice(GameData gameData, Player player, boolean accepted,
                             PendingMayAbility ability) {
        PendingReverseMiracleSearch pending = gameData.pendingReverseMiracleSearch;
        if (pending == null) {
            return;
        }
        gameData.pendingReverseMiracleSearch = null;

        Card card = ability.sourceCard();
        boolean cast = false;
        List<Card> deck = gameData.playerDecks.get(player.getId());
        if (accepted && deck != null && !deck.isEmpty()
                && deck.getLast().getId().equals(card.getId())) {
            String cost = card.getCastingOption(ReverseMiracleCast.class)
                    .map(ReverseMiracleCast::manaCostString)
                    .orElse(null);
            if (cost != null) {
                try {
                    spellCastingService.castCardFromLibraryForAlternateCostWhileSearching(
                            gameData, player, card, cost);
                    cast = true;
                } catch (IllegalStateException ex) {
                    log.info("Game {} - reverse miracle cast of {} did not happen: {}",
                            gameData.id, card.getName(), ex.getMessage());
                }
            }
        }

        LibrarySearchParams params = pending.params();
        if (cast) {
            params = params.withCards(params.cards().stream()
                    .filter(searchCard -> !searchCard.getId().equals(card.getId()))
                    .toList());
        }
        interactionHandlerRegistry.begin(gameData,
                new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                        params, pending.messagePrompt(), pending.messageCanFailToFind()));
    }
}
