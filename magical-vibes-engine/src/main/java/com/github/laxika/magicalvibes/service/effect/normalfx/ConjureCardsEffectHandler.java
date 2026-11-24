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
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Resolves {@link ConjureCardsEffect}: builds the new cards, owned by the player whose zone gets them, puts each into
 * the destination zone, and records {@link StepKind#CONJURE} with each card in the zone it is in afterwards (a
 * replacement may have sent it elsewhere). Each conjuring player's cards are one conjure event, each round of a
 * repeated conjure its own. It never prompts, so a later step of the same resolution may read it.
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
        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, conjure.count(), AmountContext.forStackEntry(entry, null)));
        List<UUID> owners = subjectResolutionService.players(gameData, entry, conjure, conjure.owners());

        List<UUID> cardIds = new ArrayList<>();
        List<MovedCard> moved = new ArrayList<>();
        List<UUID> round = owners;
        while (!round.isEmpty()) {
            Map<UUID, Integer> conjuredBy = new LinkedHashMap<>();
            List<UUID> again = new ArrayList<>();
            for (UUID ownerId : round) {
                UUID conjurerId = conjure.ownersConjure() ? ownerId : entry.getControllerId();
                int handBefore = handSize(gameData, ownerId);
                List<Group> groups = groups(gameData, entry, conjure, count);
                List<Card> made = new ArrayList<>();
                for (Group group : groups) {
                    for (Card card : group.cards()) {
                        card.setOwnerId(ownerId);
                        conjure.abilities().forEach(card::addAbility);
                        card.freeze();
                        made.add(card);
                    }
                }
                put(gameData, entry, ownerId, made, conjure.destination());
                for (Card card : made) {
                    Zone zone = zoneOf(gameData, card);
                    if (zone != null) {
                        cardIds.add(card.getId());
                        moved.add(new MovedCard(card.getId(), card.getId(), zone, ownerId));
                    }
                }
                groups.forEach(group -> log(gameData, conjurerId, ownerId, group, conjure.destination()));
                conjuredBy.merge(conjurerId, made.size(), Integer::sum);
                if (conjure.repeatWhileHandSmallerThan() != null && handSize(gameData, ownerId) > handBefore
                        && handSize(gameData, ownerId) < amountEvaluationService.evaluate(gameData,
                        conjure.repeatWhileHandSmallerThan(), AmountContext.forStackEntry(entry, null))) {
                    // "Then if you have fewer cards in hand than N, repeat this process."
                    again.add(ownerId);
                }
            }
            conjuredBy.forEach((playerId, made) -> ActionEvents.conjured(gameData, playerId, made));
            round = again;
        }
        List<UUID> players = moved.stream().map(MovedCard::ownerId).distinct().toList();
        entry.recordStep(StepKind.CONJURE, cardIds.isEmpty() ? StepOutcome.NONE
                : StepOutcome.ofObjects(cardIds, players, moved));
    }

    private static int handSize(GameData gameData, UUID playerId) {
        return gameData.playerHands.getOrDefault(playerId, List.of()).size();
    }

    /**
     * The new cards, by original: each random pick on its own, the duplicates of one original together.
     *
     * @param original the card duplicated, or {@code null} for a random pick
     * @param named    whether the log may name the cards: the original was public, or the printing is the only one
     * @param ofSpell  whether the original is a spell on the stack
     */
    private record Group(Card original, List<Card> cards, boolean named, boolean ofSpell) {
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
                // A card the text names (one printing) may be named; the cards of one printing are one line.
                boolean named = implemented.size() == 1;
                List<Card> picks = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    var printing = implemented.get(ThreadLocalRandom.current().nextInt(implemented.size()));
                    Card card = cardCatalog.findByCollectorNumber(
                            CardSet.findByCode(printing.setCode()), printing.collectorNumber()).createCard();
                    card.setConjured(true);
                    if (named) {
                        picks.add(card);
                    } else {
                        groups.add(new Group(null, List.of(card), false, false));
                    }
                }
                if (!picks.isEmpty()) {
                    groups.add(new Group(null, picks, true, false));
                }
            }
            case ConjureCardsEffect.DuplicatesOf of -> {
                boolean ofSpell = of.of() instanceof PermanentSubject.TargetSpell
                        || of.of() instanceof PermanentSubject.TriggeringSpell;
                for (Card original : subjectResolutionService.cardsToDuplicate(gameData, entry, conjure, of.of())) {
                    groups.add(new Group(original, duplicates(original, count), true, ofSpell));
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
                    groups.add(new Group(original, duplicates(original, count), named, false));
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

    /**
     * Puts {@code cards} into {@code ownerId}'s zone. Into a library the cards of one step go together: each to a
     * position of its own among the top cards when that is at random (each ends among them), the cards already there
     * keeping their order.
     */
    private void put(GameData gameData, StackEntry entry, UUID ownerId, List<Card> cards,
                     ConjureDestination destination) {
        if (cards.isEmpty()) {
            return;
        }
        switch (destination.zone()) {
            case HAND -> cards.forEach(card -> gameData.addCardToHand(ownerId, card));
            case GRAVEYARD -> cards.forEach(card -> graveyardService.addCardToGraveyard(gameData, ownerId, card));
            case EXILE -> cards.forEach(card -> {
                if (destination.exiledWithSource() && entry.getSourcePermanentId() != null) {
                    exileService.exileCard(gameData, ownerId, card, entry.getSourcePermanentId());
                } else {
                    exileService.exileCard(gameData, ownerId, card);
                }
            });
            case LIBRARY -> {
                List<Card> library = gameData.playerDecks.get(ownerId);
                if (library == null) {
                    return;
                }
                if (!destination.libraryAtRandom()) {
                    for (Card card : cards) {
                        library.add(Math.min(destination.libraryTopCards() - 1, library.size()), card);
                    }
                    return;
                }
                int slots = (int) Math.min(destination.libraryTopCards(), (long) library.size() + cards.size());
                List<Integer> positions = new ArrayList<>(IntStream.range(0, slots).boxed().toList());
                Collections.shuffle(positions, ThreadLocalRandom.current());
                List<Integer> chosen = new ArrayList<>(positions.subList(0, cards.size()));
                Collections.sort(chosen);
                for (int i = 0; i < cards.size(); i++) {
                    library.add(chosen.get(i), cards.get(i));
                }
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
     * One line per original, per random pick, or per named printing. A card is named where it goes to a public zone,
     * where it was public already, or where the text names it (CR 400.2); a library's position is not logged.
     */
    private void log(GameData gameData, UUID conjurerId, UUID ownerId, Group group, ConjureDestination destination) {
        if (group.cards().isEmpty()) {
            return;
        }
        String playerName = gameData.playerIdToName.get(conjurerId);
        String whose = ownerId.equals(conjurerId) ? "their" : gameData.playerIdToName.get(ownerId) + "'s";
        String where = switch (destination.zone()) {
            case HAND -> " into " + whose + " hand.";
            case GRAVEYARD -> " into " + whose + " graveyard.";
            case LIBRARY -> " into " + whose + " library.";
            default -> " into exile.";
        };
        boolean publicZone = destination.zone() != Zone.LIBRARY && destination.zone() != Zone.HAND;
        int size = group.cards().size();
        Card shown = group.original() != null ? group.original() : group.cards().getFirst();
        if (!publicZone && !group.named()) {
            gameLogService.append(gameData, destination.zone() == Zone.HAND && ownerId.equals(conjurerId)
                    ? GameLog.conjuresIntoHand(playerName, size)
                    : GameLog.text(playerName + " conjures " + (size == 1 ? "a card" : size + " cards") + where));
            return;
        }
        String lead = playerName + " conjures " + (group.original() != null
                ? size == 1 ? "a duplicate of " : size + " duplicates of "
                : size == 1 ? "" : size + " cards named ");
        GameLog.Builder line = GameLog.builder().text(lead);
        if (shown.getName() == null || shown.getName().isEmpty()) {
            // A nameless original is a face-down permanent's or spell's copiable values (CR 708.2a).
            line.text(group.original() == null ? Permanent.displayName(shown)
                    : group.ofSpell() ? "a face-down spell" : Permanent.FACE_DOWN_PERMANENT);
        } else {
            line.card(shown);
        }
        gameLogService.append(gameData, line.text(where).build());
    }
}
