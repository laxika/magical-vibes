package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resolves {@link ConjureCardsEffect}: builds the new cards, owned by the effect's controller, puts each into the
 * destination zone, and records {@link StepKind#CONJURE} with each card in the zone it is in afterwards (a
 * replacement may have sent it elsewhere). The cards are one conjure event. It never prompts, so a later step of the
 * same resolution may read it.
 */
@Component
@RequiredArgsConstructor
public class ConjureCardsEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final GraveyardService graveyardService;
    private final ExileService exileService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var conjure = (ConjureCardsEffect) effect;
        UUID ownerId = entry.getControllerId();
        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, conjure.count(), AmountContext.forStackEntry(entry, null)));
        List<Group> groups = groups(gameData, entry, conjure, count);

        List<UUID> cardIds = new ArrayList<>();
        List<MovedCard> moved = new ArrayList<>();
        for (Group group : groups) {
            for (Card card : group.cards()) {
                card.setOwnerId(ownerId);
                card.freeze();
                put(gameData, entry, ownerId, card, conjure.destination());
                Zone zone = zoneOf(gameData, card);
                if (zone != null) {
                    cardIds.add(card.getId());
                    moved.add(new MovedCard(card.getId(), card.getId(), zone, ownerId));
                }
            }
            log(gameData, ownerId, group, conjure.destination());
        }
        entry.recordStep(StepKind.CONJURE, cardIds.isEmpty() ? StepOutcome.NONE
                : StepOutcome.ofObjects(cardIds, List.of(ownerId), moved));
        ActionEvents.conjured(gameData, ownerId, groups.stream().mapToInt(group -> group.cards().size()).sum());
    }

    /**
     * The new cards, by original: each random pick on its own, the duplicates of one original together.
     *
     * @param original the card duplicated, or {@code null} for a random pick
     * @param named    whether the log may name the cards: the original was public, or the printing is the only one
     */
    private record Group(Card original, List<Card> cards, boolean named) {
    }

    private List<Group> groups(GameData gameData, StackEntry entry, ConjureCardsEffect conjure, int count) {
        List<Group> groups = new ArrayList<>();
        switch (conjure.cards()) {
            case ConjureCardsEffect.AtRandomFrom random -> {
                List<DraftFromSpellbookEffect.SpellbookCard> implemented = random.printings().stream()
                        .filter(this::isImplemented)
                        .toList();
                if (implemented.isEmpty()) {
                    return groups;
                }
                boolean named = implemented.size() == 1;
                for (int i = 0; i < count; i++) {
                    var printing = implemented.get(ThreadLocalRandom.current().nextInt(implemented.size()));
                    Card card = cardCatalog.findByCollectorNumber(
                            CardSet.findByCode(printing.setCode()), printing.collectorNumber()).createCard();
                    card.setConjured(true);
                    groups.add(new Group(null, List.of(card), named));
                }
            }
            case ConjureCardsEffect.DuplicatesOf of -> {
                for (Card original : subjectResolutionService.cardsToDuplicate(gameData, entry, conjure, of.of())) {
                    groups.add(new Group(original, duplicates(original, count), true));
                }
            }
            case ConjureCardsEffect.DuplicatesOfCards of -> {
                for (var located : subjectResolutionService.locatedCards(gameData, entry, conjure, of.of())) {
                    Card original = located.card();
                    if (original.isToken()) {
                        continue;
                    }
                    ExiledCardEntry exiled = located.zone() == Zone.EXILE ? gameData.findExiledCard(original.getId())
                            : null;
                    boolean named = located.zone() != Zone.HAND && located.zone() != Zone.LIBRARY
                            && (exiled == null || !exiled.faceDown());
                    groups.add(new Group(original, duplicates(original, count), named));
                }
            }
        }
        return groups;
    }

    private static List<Card> duplicates(Card original, int count) {
        List<Card> duplicates = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            duplicates.add(original.createConjuredCopy());
        }
        return duplicates;
    }

    private boolean isImplemented(DraftFromSpellbookEffect.SpellbookCard printing) {
        CardSet set = CardSet.findByCode(printing.setCode());
        return set != null && cardCatalog.getPrintings(set).stream()
                .anyMatch(candidate -> candidate.collectorNumber().equals(printing.collectorNumber()));
    }

    private void put(GameData gameData, StackEntry entry, UUID ownerId, Card card, ConjureDestination destination) {
        switch (destination.zone()) {
            case GRAVEYARD -> graveyardService.addCardToGraveyard(gameData, ownerId, card);
            case EXILE -> {
                if (destination.exiledWithSource() && entry.getSourcePermanentId() != null) {
                    exileService.exileCard(gameData, ownerId, card, entry.getSourcePermanentId());
                } else {
                    exileService.exileCard(gameData, ownerId, card);
                }
            }
            case LIBRARY -> {
                List<Card> library = gameData.playerDecks.get(ownerId);
                if (library == null) {
                    return;
                }
                int positions = Math.min(destination.libraryTopCards() - 1, library.size());
                library.add(positions == 0 ? 0 : ThreadLocalRandom.current().nextInt(positions + 1), card);
            }
            default -> throw new IllegalStateException("Not a conjure destination: " + destination.zone());
        }
    }

    /** The zone {@code card} is in, or {@code null} when it is in none a card subject names. */
    private Zone zoneOf(GameData gameData, Card card) {
        if (gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null) {
            return Zone.GRAVEYARD;
        }
        if (gameData.findExiledCard(card.getId()) != null) {
            return Zone.EXILE;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (gameData.playerDecks.getOrDefault(playerId, List.of()).contains(card)) {
                return Zone.LIBRARY;
            }
            if (gameData.playerHands.getOrDefault(playerId, List.of()).contains(card)) {
                return Zone.HAND;
            }
        }
        return null;
    }

    /**
     * One line per original, or per random pick. A card is named where it goes to a public zone, or where it was
     * public already (CR 400.2); a library's position is not logged.
     */
    private void log(GameData gameData, UUID ownerId, Group group, ConjureDestination destination) {
        if (group.cards().isEmpty()) {
            return;
        }
        String playerName = gameData.playerIdToName.get(ownerId);
        String where = switch (destination.zone()) {
            case GRAVEYARD -> " into their graveyard.";
            case LIBRARY -> " into their library.";
            default -> " into exile.";
        };
        boolean publicZone = destination.zone() != Zone.LIBRARY;
        int size = group.cards().size();
        Card shown = group.original() != null ? group.original() : group.cards().getFirst();
        if (!publicZone && !group.named()) {
            gameLogService.append(gameData, GameLog.text(playerName + " conjures "
                    + (size == 1 ? "a card" : size + " cards") + where));
            return;
        }
        String lead = playerName + " conjures " + (group.original() == null ? ""
                : size == 1 ? "a duplicate of " : size + " duplicates of ");
        GameLog.Builder line = GameLog.builder().text(lead);
        if (shown.getName() == null || shown.getName().isEmpty()) {
            // A nameless original is a face-down permanent's copiable values (CR 708.2a).
            line.text(group.original() != null ? Permanent.FACE_DOWN_PERMANENT : Permanent.displayName(shown));
        } else {
            line.card(shown);
        }
        gameLogService.append(gameData, line.text(where).build());
    }
}
