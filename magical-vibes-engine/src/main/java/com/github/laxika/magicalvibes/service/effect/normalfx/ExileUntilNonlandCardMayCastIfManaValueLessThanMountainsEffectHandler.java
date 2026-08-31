package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilNonlandCardMayCastIfManaValueLessThanMountainsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Solstice Revelations' library exile and conditional free-cast choice. */
@Component
@RequiredArgsConstructor
public class ExileUntilNonlandCardMayCastIfManaValueLessThanMountainsEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUntilNonlandCardMayCastIfManaValueLessThanMountainsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        int mountainCount = countControlledMountains(gameData, controllerId);
        List<Card> revealed = new ArrayList<>();
        Card hit = null;
        while (!library.isEmpty()) {
            Card card = library.removeFirst();
            revealed.add(card);
            exileService.exileCard(gameData, controllerId, card);
            if (!card.hasType(CardType.LAND)) {
                hit = card;
                break;
            }
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " exiles "
                + revealed.size() + " card" + (revealed.size() == 1 ? "" : "s")
                + " from the top of their library."));

        if (hit == null) {
            return;
        }

        if (hit.getManaValue() >= mountainCount) {
            putHitIntoHand(gameData, controllerId, hit, playerName);
            return;
        }

        String prompt = "You may cast " + hit.getName() + " without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, List.of(hit))
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(List.of(hit)))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING_OR_PUT_INTO_HAND)
                        .build(),
                prompt,
                true));
    }

    private int countControlledMountains(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        PermanentHasSubtypePredicate mountain = new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN);
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, mountain)) {
                count++;
            }
        }
        return count;
    }

    private void putHitIntoHand(GameData gameData, UUID controllerId, Card hit, String playerName) {
        gameData.removeFromExile(hit.getId());
        gameData.addCardToHand(controllerId, hit);
        gameLogService.append(gameData, GameLog.cardThen(hit,
                " is put into " + playerName + "'s hand."));
    }
}
