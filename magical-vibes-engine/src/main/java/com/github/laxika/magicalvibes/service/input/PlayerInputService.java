package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction.MultiZoneExileChoice.SearchedZones;
import com.github.laxika.magicalvibes.model.PowerToughnessForm;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeChoiceOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.turn.UntapStepService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerInputService {
    @Autowired
    @org.springframework.context.annotation.Lazy
    private UntapStepService untapStepService;

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector triggerTargetCollector;

    @Autowired @Lazy
    private BattlefieldEntryService battlefieldEntryService;

    @Autowired @Lazy
    private InputCompletionService inputCompletionService;

    @Autowired @Lazy
    private com.github.laxika.magicalvibes.service.battlefield.GameQueryService gameQueryService;

    @Autowired(required = false)
    private CardCatalog cardCatalog;
    @Autowired(required = false)
    private CardNameCatalog cardNameCatalog;
    private volatile List<CardPrinting> implementedPrintings;
    private volatile CatalogNames catalogNames;

    private record CardNameFilter(List<CardType> excludedTypes, CardType requiredType) {}

    /**
     * What a name prompt reads off one named card class of the catalog. Every catalog name list is
     * filtered from these, so the catalog's cards are built once rather than once per list.
     *
     * @param tokenOnly whether the card is a token that is not also a card, whose name is no card name
     */
    private record CatalogCard(List<String> names, CardType type, Set<CardType> additionalTypes,
                               Set<CardSupertype> supertypes, boolean tokenOnly) {

        boolean hasType(CardType cardType) {
            return type == cardType || additionalTypes.contains(cardType);
        }

        boolean isBasicLand() {
            return hasType(CardType.LAND) && supertypes.contains(CardSupertype.BASIC);
        }

        boolean isNameCandidate(List<CardType> excludedTypes, CardType requiredType) {
            return type != null && !tokenOnly && excludedTypes.stream().noneMatch(this::hasType)
                    && (requiredType == null || hasType(requiredType));
        }
    }

    /**
     * The name lists of one set of printings, each built on first use and kept. A class registered
     * at several printings is built once: its name and types come from its oracle data, which is
     * the same at each of them.
     */
    private static final class CatalogNames {

        private final List<CardPrinting> printings;
        private volatile List<CatalogCard> cards;
        private volatile List<String> nonbasicLandNames;
        private volatile List<String> nonbasicNames;
        private final Map<CardNameFilter, List<String>> filteredNames = new java.util.concurrent.ConcurrentHashMap<>();

        CatalogNames(List<CardPrinting> printings) {
            this.printings = printings;
        }

        List<String> matching(CardNameFilter filter) {
            return filteredNames.computeIfAbsent(filter, key ->
                    names(card -> card.isNameCandidate(key.excludedTypes(), key.requiredType())));
        }

        List<String> nonbasic() {
            List<String> cached = nonbasicNames;
            if (cached == null) {
                cached = names(card -> !card.isBasicLand());
                nonbasicNames = cached;
            }
            return cached;
        }

        List<String> nonbasicLands() {
            List<String> cached = nonbasicLandNames;
            if (cached == null) {
                cached = names(card -> card.hasType(CardType.LAND) && !card.isBasicLand());
                nonbasicLandNames = cached;
            }
            return cached;
        }

        /** The sorted, distinct names of the cards that satisfy {@code candidate}. */
        private List<String> names(Predicate<CatalogCard> candidate) {
            Set<String> names = new TreeSet<>();
            for (CatalogCard card : cards()) {
                if (candidate.test(card)) {
                    names.addAll(card.names());
                }
            }
            return List.copyOf(names);
        }

        private List<CatalogCard> cards() {
            List<CatalogCard> cached = cards;
            if (cached != null) {
                return cached;
            }
            Set<String> seenClassNames = new HashSet<>();
            List<CatalogCard> built = new ArrayList<>();
            for (CardPrinting printing : printings) {
                if (!seenClassNames.add(printing.cardClassName())) {
                    continue;
                }
                Card card = printing.createCard();
                if (card.getName() != null) {
                    built.add(new CatalogCard(card.getNames(), card.getType(), Set.copyOf(card.getAdditionalTypes()),
                            Set.copyOf(card.getSupertypes()), card.isToken() && !card.isTokenCard()));
                }
            }
            List<CatalogCard> result = List.copyOf(built);
            cards = result;
            return result;
        }
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        beginCardChoice(gameData, playerId, validIndices, prompt, false);
    }

    public void beginPerpetualCreatureCardChoice(GameData gameData, UUID playerId,
                                                  List<Integer> validIndices, String prompt,
                                                  int powerBoost, Set<Keyword> keywords) {
        beginPerpetualCreatureCardChoice(gameData, playerId, validIndices, prompt,
                powerBoost, keywords, false);
    }

    public void beginPerpetualCreatureCardChoice(GameData gameData, UUID playerId,
                                                  List<Integer> validIndices, String prompt,
                                                  int powerBoost, Set<Keyword> keywords,
                                                  boolean grantBlitz) {
        beginPerpetualCreatureCardChoice(gameData, playerId, validIndices, prompt, powerBoost, 0, keywords, grantBlitz);
    }

    public void beginPerpetualCreatureCardChoice(GameData gameData, UUID playerId,
                                                  List<Integer> validIndices, String prompt,
                                                  int powerBoost, int toughnessBoost, Set<Keyword> keywords,
                                                  boolean grantBlitz) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualCreatureCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, powerBoost, toughnessBoost, keywords, grantBlitz));
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, boolean enterTapped) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, enterTapped));
    }

    public void beginCommandZoneCardChoice(GameData gameData, UUID playerId, List<Card> cards, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.CommandZoneCardChoice(
                playerId, cards.stream().map(Card::getId).toList(), prompt));
    }

    public void beginCommandZoneCardChoice(GameData gameData, UUID playerId, List<Card> cards, String prompt,
                                           Card tokenCopySourceCard) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.CommandZoneCardChoice(
                playerId, cards.stream().map(Card::getId).toList(), prompt, tokenCopySourceCard));
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, null);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, false, null, null, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                                String drawAndRepeatLabel) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                                String drawAndRepeatLabel, boolean putAnyNumber) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel,
                putAnyNumber, false, 0, 0, Set.of(), null);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                                String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate,
                drawAndRepeatLabel, putAnyNumber, faceDown, faceDownPower, faceDownToughness,
                faceDownCardTypes, null);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                                String drawAndRepeatLabel, boolean putAnyNumber, UUID returnExiledSourceCardId) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate,
                drawAndRepeatLabel, putAnyNumber, false, 0, 0, Set.of(), returnExiledSourceCardId);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                                String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate,
                drawAndRepeatLabel, putAnyNumber, faceDown, faceDownPower, faceDownToughness,
                faceDownCardTypes, returnExiledSourceCardId, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                                String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId,
                                boolean returnToHandAtEndStep) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate,
                drawAndRepeatLabel, putAnyNumber, faceDown, faceDownPower, faceDownToughness,
                faceDownCardTypes, returnExiledSourceCardId, returnToHandAtEndStep,
                false, null, null, null, null);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                CardPredicate drawAndRepeatPredicate, String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId,
                                boolean returnToHandAtEndStep, CardPredicate enterTappedAndAttackingIf) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate,
                drawAndRepeatLabel, putAnyNumber, faceDown, faceDownPower, faceDownToughness,
                faceDownCardTypes, returnExiledSourceCardId, returnToHandAtEndStep,
                false, null, null, enterTappedAndAttackingIf, null);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                CardPredicate drawAndRepeatPredicate, String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId,
                                boolean returnToHandAtEndStep, CardEffect thenEffect, CardPredicate thenCondition) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate,
                drawAndRepeatLabel, putAnyNumber, faceDown, faceDownPower, faceDownToughness,
                faceDownCardTypes, returnExiledSourceCardId, returnToHandAtEndStep,
                false, thenEffect, thenCondition, null, null);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                CardPredicate drawAndRepeatPredicate, String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId,
                                boolean returnToHandAtEndStep, UUID blockingAttackerId) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate,
                drawAndRepeatLabel, putAnyNumber, faceDown, faceDownPower, faceDownToughness,
                faceDownCardTypes, returnExiledSourceCardId, returnToHandAtEndStep,
                false, null, null, null, blockingAttackerId);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                CardPredicate drawAndRepeatPredicate, String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId,
                                boolean returnToHandAtEndStep, boolean cloaked,
                                CardEffect thenEffect, CardPredicate thenCondition,
                                CardPredicate enterTappedAndAttackingIf,
                                UUID blockingAttackerId) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel, putAnyNumber, faceDown, faceDownPower, faceDownToughness, faceDownCardTypes, returnExiledSourceCardId, returnToHandAtEndStep, cloaked, thenEffect, thenCondition, enterTappedAndAttackingIf, blockingAttackerId, null, Set.of());
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                CardPredicate drawAndRepeatPredicate, String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId,
                                boolean returnToHandAtEndStep, boolean cloaked,
                                CardEffect thenEffect, CardPredicate thenCondition,
                                CardPredicate enterTappedAndAttackingIf,
                                UUID blockingAttackerId, UUID untapSourcePermanentId,
                                Set<CardSubtype> untapSourceIfEnteredCardHasAnySubtype) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel,
                putAnyNumber, faceDown, faceDownPower, faceDownToughness, faceDownCardTypes,
                returnExiledSourceCardId, returnToHandAtEndStep, cloaked, thenEffect, thenCondition,
                enterTappedAndAttackingIf, blockingAttackerId, untapSourcePermanentId,
                untapSourceIfEnteredCardHasAnySubtype, false);
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                CardPredicate drawAndRepeatPredicate, String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId,
                                boolean returnToHandAtEndStep, boolean cloaked,
                                CardEffect thenEffect, CardPredicate thenCondition,
                                CardPredicate enterTappedAndAttackingIf,
                                UUID blockingAttackerId, UUID untapSourcePermanentId,
                                Set<CardSubtype> untapSourceIfEnteredCardHasAnySubtype,
                                boolean markEntryAsPutByTriggeringPermanent) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel,
                putAnyNumber, faceDown, faceDownPower, faceDownToughness, faceDownCardTypes,
                returnExiledSourceCardId, returnToHandAtEndStep, cloaked, thenEffect, thenCondition,
                enterTappedAndAttackingIf, blockingAttackerId, untapSourcePermanentId,
                untapSourceIfEnteredCardHasAnySubtype, markEntryAsPutByTriggeringPermanent, false);
    }

    /** As above, where {@code mandatory} forbids declining the choice (an instruction to put this card onto the battlefield). */
    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                CardPredicate drawAndRepeatPredicate, String drawAndRepeatLabel, boolean putAnyNumber,
                                boolean faceDown, int faceDownPower, int faceDownToughness,
                                Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId,
                                boolean returnToHandAtEndStep, boolean cloaked,
                                CardEffect thenEffect, CardPredicate thenCondition,
                                CardPredicate enterTappedAndAttackingIf,
                                UUID blockingAttackerId, UUID untapSourcePermanentId,
                                Set<CardSubtype> untapSourceIfEnteredCardHasAnySubtype,
                                boolean markEntryAsPutByTriggeringPermanent, boolean mandatory) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                attachEquipmentCardId, enterAttacking, null, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel,
                putAnyNumber, faceDown, faceDownPower, faceDownToughness, faceDownCardTypes,
                returnExiledSourceCardId, null, null, 0, returnToHandAtEndStep,
                cloaked, thenEffect, thenCondition, enterTappedAndAttackingIf, blockingAttackerId,
                untapSourcePermanentId, untapSourceIfEnteredCardHasAnySubtype, null, 0, null,
                markEntryAsPutByTriggeringPermanent, mandatory));
    }

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                boolean enterTapped, boolean grantHaste, boolean sacrificeAtEndStep,
                                UUID attachEquipmentCardId, boolean enterAttacking, boolean drawAndRepeat,
                                CardPredicate drawAndRepeatPredicate, String drawAndRepeatLabel,
                                boolean putAnyNumber, boolean faceDown, int faceDownPower,
                                int faceDownToughness, Set<CardType> faceDownCardTypes,
                                UUID returnExiledSourceCardId, boolean returnToHandAtEndStep,
                                boolean cloaked, CardEffect thenEffect, CardPredicate thenCondition) {
        beginCardChoice(gameData, playerId, validIndices, prompt, enterTapped, grantHaste,
                sacrificeAtEndStep, attachEquipmentCardId, enterAttacking, drawAndRepeat,
                drawAndRepeatPredicate, drawAndRepeatLabel, putAnyNumber, faceDown,
                faceDownPower, faceDownToughness, faceDownCardTypes, returnExiledSourceCardId,
                returnToHandAtEndStep, cloaked, thenEffect, thenCondition, null, null);
    }

    public void beginCardChoiceWithArtifactCounters(GameData gameData, UUID playerId, List<Integer> validIndices,
                                                     String prompt, CounterType counterType, int counterCount) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, false, false, false, null, false, null,
                false, null, null, false, false, 0, 0, Set.of(), null, null, counterType, counterCount));
    }

    public void beginCardChoiceWithEntryCounters(GameData gameData, UUID playerId, List<Integer> validIndices,
                                                  String prompt, CounterType counterType, int counterCount,
                                                  CardPredicate condition) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, false, false, false, null, false, null,
                false, null, null, false, false, 0, 0, Set.of(), null, null, null, 0, false,
                false, null, null, null, null, null, Set.of(), counterType, counterCount, condition));
    }

    public void beginCardChoiceThenReturnSourceToHand(GameData gameData, UUID playerId, List<Integer> validIndices,
                                                       String prompt, UUID sourcePermanentId) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, false, false, false, null, false, null,
                false, null, null, false, sourcePermanentId));
    }

    /** Flash-style: choose a creature to put onto the battlefield, then pay its cost reduced by N or sacrifice it. */
    public void beginCardChoiceSacrificeUnlessPayReduced(GameData gameData, UUID playerId, List<Integer> validIndices,
                                                         String prompt, int genericReduction) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HandCardChoice(
                playerId, new ArrayList<>(validIndices), prompt, Integer.valueOf(genericReduction)));
    }

    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID targetId) {
        beginTargetedCardChoice(gameData, playerId, validIndices, prompt, targetId, null);
    }

    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices,
                                        String prompt, UUID targetId, boolean declinable) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.TargetedHandCardChoice(
                playerId, new ArrayList<>(validIndices), targetId, prompt, null, null, declinable));
    }

    /**
     * Variant where declining the choice exiles the given permanent (e.g. Evershrike: "You may put an
     * Aura ... onto the battlefield attached to it. If you don't, exile this creature.").
     */
    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                        UUID targetId, UUID exileSourceIfDeclinedId) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.TargetedHandCardChoice(
                playerId, new ArrayList<>(validIndices), targetId, prompt, exileSourceIfDeclinedId, null));
    }

    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                        UUID targetId, UUID exileSourceIfDeclinedId, UUID returnSourceToHandId) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.TargetedHandCardChoice(
                playerId, new ArrayList<>(validIndices), targetId, prompt,
                exileSourceIfDeclinedId, returnSourceToHandId));
    }

    private List<UUID> choosablePermanentIds(GameData gameData, List<UUID> permanentIds) {
        return permanentIds.stream()
                .filter(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent == null || !gameQueryService.cantBeChosen(gameData, permanent);
                })
                .toList();
    }

    public void beginPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentChoice(
                playerId, new ArrayList<>(choosablePermanentIds(gameData, validIds)), List.of(),
                gameData.interaction.permanentChoiceContext(), prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent", gameData.id, playerName);
    }

    public void beginPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds,
                                     PermanentChoiceContext context, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentChoice(
                playerId, new ArrayList<>(choosablePermanentIds(gameData, validIds)), List.of(), context, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent", gameData.id, playerName);
    }

    public void beginAnyTargetChoice(GameData gameData, UUID playerId, List<UUID> validPermanentIds, List<UUID> validPlayerIds, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentChoice(
                playerId, new ArrayList<>(validPermanentIds), new ArrayList<>(validPlayerIds),
                gameData.interaction.permanentChoiceContext(), prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose any target", gameData.id, playerName);
    }

    public void beginPlayerChoice(GameData gameData, UUID playerId, List<UUID> validPlayerIds, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentChoice(
                playerId, List.of(), new ArrayList<>(validPlayerIds),
                gameData.interaction.permanentChoiceContext(), prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a player", gameData.id, playerName);
    }

    public void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount, String prompt) {
        beginMultiPermanentChoice(gameData, playerId, validIds, maxCount, null, prompt);
    }

    public void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount,
                                          MultiPermanentChoiceContext context, String prompt) {
        beginMultiPermanentChoice(gameData, playerId, validIds, List.of(), maxCount, context, prompt);
    }

    public void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds,
                                          List<UUID> validCardIds, int maxCount,
                                          MultiPermanentChoiceContext context, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiPermanentChoice(
                playerId, new ArrayList<>(choosablePermanentIds(gameData, validIds)), List.of(), new ArrayList<>(validCardIds),
                maxCount, context, prompt));
    }

    public void beginMultiPermanentOrPlayerChoice(GameData gameData, UUID playerId,
                                                   List<UUID> validPermanentIds,
                                                   List<UUID> validPlayerIds, int maxCount,
                                                   MultiPermanentChoiceContext context, String prompt) {
        beginMultiPermanentOrPlayerChoice(gameData, playerId, validPermanentIds, validPlayerIds, List.of(),
                maxCount, context, prompt);
    }

    /** Selects any mix of permanents, players and graveyard or exiled cards, up to {@code maxCount}. */
    public void beginMultiPermanentOrPlayerChoice(GameData gameData, UUID playerId,
                                                   List<UUID> validPermanentIds,
                                                   List<UUID> validPlayerIds, List<UUID> validCardIds,
                                                   int maxCount, MultiPermanentChoiceContext context,
                                                   String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiPermanentChoice(
                playerId, new ArrayList<>(validPermanentIds), new ArrayList<>(validPlayerIds),
                new ArrayList<>(validCardIds), maxCount, context, prompt));
    }

    /** Asks the spell's controller whether rebound or plot replaces the spell going to the graveyard (CR 616.1). */
    public void beginSpellReboundOrPlotChoice(GameData gameData, ChoiceContext.SpellReboundOrPlotChoice context) {
        var choice = new PendingInteraction.ColorChoice(
                context.controllerId(), null, null, context, List.of("REBOUND", "PLOT"),
                "Choose whether " + context.card().getName() + " is exiled with rebound or becomes plotted.");
        if (gameData.interaction.isAwaitingInput()) {
            gameData.pendingInteractions.addLast(choice);
        } else {
            interactionHandlerRegistry.begin(gameData, choice);
        }
    }

    /** Asks the spell's controller whether rebound or a dream-counter exile replaces the spell going to the graveyard (CR 616.1). */
    public void beginSpellReboundOrDreamCounterChoice(GameData gameData,
                                                      ChoiceContext.SpellReboundOrDreamCounterChoice context) {
        var choice = new PendingInteraction.ColorChoice(
                context.controllerId(), null, null, context, List.of("REBOUND", "DREAM_COUNTER"),
                "Choose whether " + context.card().getName() + " is exiled with rebound or with a dream counter.");
        if (gameData.interaction.isAwaitingInput()) {
            gameData.pendingInteractions.addLast(choice);
        } else {
            interactionHandlerRegistry.begin(gameData, choice);
        }
    }

    /** Asks the spell's controller which of several exile replacements applies to the resolving spell (CR 616.1). */
    public void beginSpellExileSourceChoice(GameData gameData, ChoiceContext.SpellExileSourceChoice context,
                                            List<UUID> sourcePermanentIds) {
        var choice = new PendingInteraction.ColorChoice(
                context.controllerId(), null, null, context,
                sourcePermanentIds.stream().map(UUID::toString).toList(),
                "Choose which permanent exiles " + context.card().getName() + " instead of putting it into a graveyard.");
        if (gameData.interaction.isAwaitingInput()) {
            gameData.pendingInteractions.addLast(choice);
        } else {
            interactionHandlerRegistry.begin(gameData, choice);
        }
    }

    public void beginSagaChapterCounterAssignmentChoice(
            GameData gameData, UUID playerId, ChoiceContext.SagaChapterCounterAssignment context) {
        int assigned = context.assignments().values().stream().mapToInt(Integer::intValue).sum();
        int remaining = context.total() - assigned;
        int remainingTargets = context.targetIds().size() - context.nextTargetIndex();
        List<String> options = counterAssignmentOptions(remaining, remainingTargets, false);
        String prompt = context.counterType() == null
                ? "Choose how much damage to deal to this target."
                : "Choose how many " + context.counterType().name().toLowerCase().replace('_', ' ')
                + " counters to put on the target creature.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, options, prompt));
    }

    public void beginCounterDistributionAssignmentChoice(
            GameData gameData, UUID playerId, ChoiceContext.CounterDistributionAssignment context) {
        int assigned = context.assignments().values().stream().mapToInt(Integer::intValue).sum();
        int remaining = context.total() - assigned;
        int remainingTargets = context.targetIds().size() - context.nextTargetIndex();
        List<String> options = counterAssignmentOptions(remaining, remainingTargets,
                context.allowsPartialDistribution());
        boolean prevention = context.counterType() == null && context.effects().stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect.class::isInstance);
        String prompt = prevention
                ? "Choose how much damage to prevent for this target."
                : context.counterType() == null
                ? "Choose how much damage to deal to this target."
                : "Choose how many " + context.counterType().name().toLowerCase().replace('_', ' ')
                + " counters to put on the target creature.";
        boolean announcedAtResolution = context.counterType() == null && context.effects().stream()
                .anyMatch(effect -> effect instanceof com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect divided
                        && divided.etbAssignments() && divided.xValueDivisionPrompt());
        if (announcedAtResolution) {
            int maxForTarget = remaining - (remainingTargets - 1);
            int minForTarget = !context.allowsPartialDistribution() && remainingTargets == 1 ? remaining : 1;
            interactionHandlerRegistry.begin(gameData, PendingInteraction.XValueChoice.forDistribution(
                    playerId, minForTarget, maxForTarget, prompt, context.sourceCard().getName(), context));
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, options, prompt));
    }

    private static List<String> counterAssignmentOptions(int remaining, int remainingTargets,
                                                          boolean allowsPartialDistribution) {
        int minForTarget = !allowsPartialDistribution && remainingTargets == 1 ? remaining : 1;
        int maxForTarget = remaining - (remainingTargets - 1);
        return IntStream.rangeClosed(minForTarget, maxForTarget)
                .mapToObj(Integer::toString)
                .toList();
    }

    public void beginMultiGraveyardChoice(GameData gameData, UUID playerId, List<Card> cards, int maxCount, String prompt) {
        beginMultiGraveyardChoice(gameData, playerId, cards, maxCount, 0, prompt);
    }

    public void beginMultiGraveyardChoice(GameData gameData, UUID playerId, List<Card> cards,
                                          int maxCount, int minCount, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                playerId, new ArrayList<>(cards), maxCount, prompt, minCount));
    }

    public void beginMultiGraveyardChoice(GameData gameData, UUID playerId, List<Card> cards,
                                          int maxCount, int minCount, Integer maxTotalManaValue,
                                          String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                playerId, new ArrayList<>(cards), maxCount, prompt, minCount, maxTotalManaValue));
    }

    public void beginMultiGraveyardChoiceWithMinimumManaValue(GameData gameData, UUID playerId,
                                                               List<Card> cards, int maxCount,
                                                               int minimumTotalManaValue, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                playerId, new ArrayList<>(cards), maxCount, prompt, 0, false,
                minimumTotalManaValue, null));
    }

    public void beginMultiGraveyardChoiceWithMaximumManaValue(GameData gameData, UUID playerId,
                                                               List<Card> cards, int maxCount,
                                                               int minCount, int maximumTotalManaValue,
                                                               String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                playerId, new ArrayList<>(cards), maxCount, prompt, minCount, maximumTotalManaValue));
    }

    public void beginMultiGraveyardChoiceWithMaximumPower(GameData gameData, UUID playerId,
                                                           List<Card> cards, int maxCount,
                                                           int minCount, int maximumTotalPower,
                                                           String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                playerId, new ArrayList<>(cards), maxCount, prompt, minCount, false,
                null, null, maximumTotalPower));
    }

    public void beginAsEntersCounterTypeChoice(GameData gameData,
                                                GraveyardTargetOperationState.AsEntersGraveyardExileContext context,
                                                int exiledCardCount) {
        ChoiceContext.AsEntersCounterTypeChoice choiceContext = new ChoiceContext.AsEntersCounterTypeChoice(
                context.enteringPermanentId(), context.controllerId(), context.card(), context.targetId(),
                context.wasCastFromHand(), context.etbMode(), context.xValue(), context.kicked(),
                context.targetIds(), exiledCardCount, context.counterTypes());
        beginAsEntersCounterTypeChoice(gameData, choiceContext);
    }

    public void beginAsEntersCounterTypeChoice(GameData gameData,
                                                ChoiceContext.AsEntersCounterTypeChoice context) {
        List<String> options = context.counterTypes().stream()
                .map(ChoiceContext.AsEntersCounterTypeChoice::label)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                context.controllerId(), null, null, context, options,
                context.card().getName() + " — Choose a counter type."));
    }

    public void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetId) {
        beginColorChoice(gameData, playerId, permanentId, etbTargetId,
                List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN));
    }

    /**
     * As {@link #beginColorChoice(GameData, UUID, UUID, UUID)} but restricted to {@code allowedColors}
     * — for cards that narrow the pick ("choose black or red", Mangara's Equity).
     */
    public void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetId,
            List<CardColor> allowedColors) {
        List<String> colors = allowedColors.stream().map(Enum::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, permanentId, etbTargetId, null, colors,
                colors.size() < 5 ? "Choose " + String.join(" or ", colors).toLowerCase() + "." : "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
    }

    public void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetId,
            ChooseColorEffect choice) {
        if (choice.colorsFromControllerHand()) {
            List<CardColor> handColors = List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                            CardColor.RED, CardColor.GREEN).stream()
                    .filter(color -> gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                            .anyMatch(card -> card.getColors() != null && card.getColors().contains(color)))
                    .toList();
            if (handColors.isEmpty()) {
                Permanent permanent = gameData.playerBattlefields.values().stream()
                        .flatMap(List::stream)
                        .filter(candidate -> candidate.getId().equals(permanentId))
                        .findFirst()
                        .orElse(null);
                if (permanent != null) {
                    permanent.setChosenColorChoiceMade(true);
                    battlefieldEntryService.processCreatureETBEffects(gameData, playerId,
                            permanent.getCard(), etbTargetId, false);
                }
                return;
            }
            beginUpToTwoColorsOnEnterChoice(gameData, playerId, permanentId, etbTargetId,
                    handColors, List.of());
            return;
        }
        if (choice.choicesRequired() == 2) {
            beginTwoColorsOnEnterChoice(gameData, playerId, permanentId, etbTargetId, List.of());
            return;
        }
        beginColorChoice(gameData, playerId, permanentId, etbTargetId, choice.allowedColors());
    }

    public void beginTwoColorsOnEnterChoice(GameData gameData, UUID playerId, UUID permanentId,
            UUID etbTargetId, List<CardColor> chosen) {
        ChoiceContext.ChooseTwoColorsOnEnterChoice ctx =
                new ChoiceContext.ChooseTwoColorsOnEnterChoice(permanentId, etbTargetId, new ArrayList<>(chosen));

        List<String> options = new ArrayList<>();
        for (CardColor color : List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN)) {
            if (!chosen.contains(color)) {
                options.add(color.name());
            }
        }
        String prompt = chosen.isEmpty() ? "Choose two colors." : "Choose a second color.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, options, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose two colors", gameData.id, playerName);
    }

    public void beginUpToTwoColorsOnEnterChoice(GameData gameData, UUID playerId, UUID permanentId,
            UUID etbTargetId, List<CardColor> allowedColors, List<CardColor> chosen) {
        ChoiceContext.ChooseUpToTwoColorsOnEnterChoice ctx =
                new ChoiceContext.ChooseUpToTwoColorsOnEnterChoice(
                        permanentId, etbTargetId, allowedColors, new ArrayList<>(chosen));

        List<String> options = new ArrayList<>();
        for (CardColor color : allowedColors) {
            if (!chosen.contains(color)) {
                options.add(color.name());
            }
        }
        options.add("DONE");
        String prompt = chosen.isEmpty() ? "Choose up to two colors." : "Choose another color, or DONE.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, options, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to two colors", gameData.id, playerName);
    }

    public void beginDiscardChosenColorChoice(GameData gameData, UUID controllerId, UUID targetPlayerId) {
        ChoiceContext.DiscardChosenColorChoice ctx = new ChoiceContext.DiscardChosenColorChoice(controllerId, targetPlayerId);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (discard all cards of that color)", gameData.id, playerName);
    }

    public void beginExileTopCardsChosenColorTokensChoice(GameData gameData, UUID controllerId, UUID targetPlayerId,
            int count, com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate, String sourceSetCode) {
        ChoiceContext.ExileTopCardsChosenColorTokensChoice ctx =
                new ChoiceContext.ExileTopCardsChosenColorTokensChoice(controllerId, targetPlayerId, count, tokenTemplate, sourceSetCode);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (Oona-style exile/token)", gameData.id, playerName);
    }

    /**
     * Asks for Jinnie Fay's Cat, Dog or original tokens as her replacement applies to a token creation event,
     * queued behind any prompt already awaiting an answer.
     */
    public void beginJinnieFayTokenChoice(GameData gameData, ChoiceContext.JinnieFayTokenChoice ctx) {
        List<String> options = List.of(
                com.github.laxika.magicalvibes.model.effect.JinnieFayTokenReplacementEffect.CAT_OPTION,
                com.github.laxika.magicalvibes.model.effect.JinnieFayTokenReplacementEffect.DOG_OPTION,
                com.github.laxika.magicalvibes.model.effect.JinnieFayTokenReplacementEffect.ORIGINAL_OPTION);
        PendingInteraction.ColorChoice choice = new PendingInteraction.ColorChoice(
                ctx.controllerId(), null, null, ctx, options,
                ctx.sourceCard().getName() + " — Choose Cat, Dog, or original tokens.");
        if (gameData.interaction.isAwaitingInput()) {
            gameData.queueInteraction(choice);
        } else {
            interactionHandlerRegistry.begin(gameData, choice);
        }
    }

    public void beginGainLifePerPermanentOfChosenColorChoice(GameData gameData, UUID controllerId,
            Card sourceCard, StackEntryType sourceEntryType) {
        ChoiceContext.GainLifePerPermanentOfChosenColorChoice ctx =
                new ChoiceContext.GainLifePerPermanentOfChosenColorChoice(controllerId, sourceCard, sourceEntryType);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (Treva life-per-permanent)", gameData.id, playerName);
    }

    /**
     * Hall of Gemstone: {@code playerId} chooses the color every land produces for the rest of the
     * turn.
     */
    public void beginAllLandsProduceChosenColorChoice(GameData gameData, UUID playerId, String sourceCardName,
                                                      UUID sourcePermanentId, UUID controllerId) {
        ChoiceContext.AllLandsProduceChosenColorChoice ctx = new ChoiceContext.AllLandsProduceChosenColorChoice(
                playerId, sourceCardName, sourcePermanentId, controllerId);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color (all lands produce that color)", gameData.id, playerName);
    }

    public void beginProtectionColorChoice(GameData gameData, UUID playerId, UUID targetId, boolean includeArtifacts) {
        beginProtectionColorChoice(gameData, playerId, List.of(targetId), includeArtifacts);
    }

    public void beginPreventDamageToTargetFromChosenColorChoice(GameData gameData, UUID playerId, UUID targetId) {
        ChoiceContext.PreventDamageToTargetFromChosenColorChoice context =
                new ChoiceContext.PreventDamageToTargetFromChosenColorChoice(targetId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context,
                List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN"),
                "Choose a color."));
    }

    /**
     * One protection pick shared by several targets (Prismatic Boon's "X target creatures gain
     * protection from the chosen color").
     */
    public void beginProtectionColorChoice(GameData gameData, UUID playerId, List<UUID> targetIds, boolean includeArtifacts) {
        beginProtectionColorChoice(gameData, playerId, targetIds, includeArtifacts, false);
    }

    public void beginProtectionColorChoice(GameData gameData, UUID playerId, List<UUID> targetIds,
                                           boolean includeArtifacts, boolean includeColorless) {
        ChoiceContext.ProtectionColorChoice ctx =
                new ChoiceContext.ProtectionColorChoice(targetIds, includeArtifacts, includeColorless);

        List<String> options = new java.util.ArrayList<>(List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN"));
        if (includeArtifacts) {
            options.addFirst("ARTIFACT");
        }
        if (includeColorless) {
            options.add("COLORLESS");
        }
        String prompt = includeArtifacts && includeColorless
                ? "Choose a color, colorless, or artifacts."
                : includeArtifacts
                ? "Choose a color or artifacts."
                : includeColorless
                ? "Choose a color or colorless."
                : "Choose a color.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, options, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose protection", gameData.id, playerName);
    }

    public void beginColorSetChoice(GameData gameData, UUID controllerId, UUID targetId, String sourceCardName) {
        ChoiceContext.ColorSetChoice ctx = new ChoiceContext.ColorSetChoice(targetId, controllerId, sourceCardName);

        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a color (target becomes chosen color)", gameData.id, playerName);
    }

    /**
     * Prompts {@code playerId} to pick the colors {@code targetIds} become. With {@code oneColor} one color is picked;
     * otherwise colors are picked one at a time: already-chosen colors are dropped from the options, and "DONE" is
     * offered once at least one color has been chosen. The choice handler accumulates the picks and re-invokes this
     * until the player is done (see {@code ChoiceHandlerService}).
     */
    public void beginBecomeChosenColorsChoice(GameData gameData, UUID playerId, List<UUID> targetIds,
                                              String sourceCardName, List<CardColor> chosen,
                                              Duration duration, boolean oneColor) {
        ChoiceContext.BecomeChosenColorsChoice ctx =
                new ChoiceContext.BecomeChosenColorsChoice(new ArrayList<>(targetIds), sourceCardName,
                        new ArrayList<>(chosen), duration, oneColor);

        List<String> options = new ArrayList<>();
        for (CardColor color : List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN)) {
            if (!chosen.contains(color)) {
                options.add(color.name());
            }
        }
        if (!chosen.isEmpty() && !oneColor) {
            options.add("DONE");
        }
        String prompt = chosen.isEmpty() ? "Choose a color." : "Choose another color, or DONE.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, options, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
    }

    public void beginMassProtectionColorChoice(GameData gameData, UUID controllerId, String sourceCardName) {
        ChoiceContext.MassProtectionColorChoice ctx =
                new ChoiceContext.MassProtectionColorChoice(controllerId, sourceCardName);

        List<String> options = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, options, "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose protection (you and your permanents)", gameData.id, playerName);
    }

    public void beginControllerProtectionColorChoice(GameData gameData, UUID controllerId, String sourceCardName) {
        ChoiceContext.ControllerProtectionColorChoice ctx =
                new ChoiceContext.ControllerProtectionColorChoice(controllerId, sourceCardName);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx,
                List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN"), "Choose a color."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose protection", gameData.id, playerName);
    }

    public void beginChooseModeChoice(GameData gameData, UUID controllerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect) {
        beginChooseModeChoice(gameData, controllerId, sourceCard, effect, null);
    }

    /**
     * Mode pick for a modal ability as it resolves: the chosen mode's effects are spliced into the resolution.
     * {@code sourcePermanentId} is the permanent whose counters a mode may remove.
     */
    public void beginChooseModeChoice(GameData gameData, UUID controllerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, UUID sourcePermanentId) {
        beginChooseModeChoice(gameData, controllerId, sourceCard, effect, sourcePermanentId, false, List.of());
    }

    public void beginChooseModeChoice(GameData gameData, UUID controllerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, UUID sourcePermanentId,
            boolean consumeMode) {
        beginChooseModeChoice(gameData, controllerId, sourceCard, effect, sourcePermanentId, consumeMode, List.of());
    }

    public void beginChooseModeChoice(GameData gameData, UUID controllerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, UUID sourcePermanentId,
            List<String> chosenLabels) {
        beginChooseModeChoice(gameData, controllerId, sourceCard, effect, sourcePermanentId, false, chosenLabels);
    }

    public void beginChooseModeChoice(GameData gameData, UUID controllerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, UUID sourcePermanentId,
            boolean consumeMode, List<String> chosenLabels) {
        beginChooseModeChoice(gameData, new ChoiceContext.ChooseModeChoice(sourceCard, controllerId, effect,
                sourcePermanentId, consumeMode, chosenLabels, false));
    }

    /**
     * {@link #beginChooseModeChoice(GameData, UUID, Card, com.github.laxika.magicalvibes.model.effect.ChooseOneEffect,
     * UUID)} for a choice the other players don't learn ({@link ChoiceContext.ChooseModeChoice#secret()}).
     */
    public void beginSecretChooseModeChoice(GameData gameData, UUID controllerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, UUID sourcePermanentId) {
        beginChooseModeChoice(gameData, new ChoiceContext.ChooseModeChoice(sourceCard, controllerId, effect,
                sourcePermanentId, false, List.of(), false, true));
    }

    private void beginChooseModeChoice(GameData gameData, ChoiceContext.ChooseModeChoice ctx) {
        UUID controllerId = ctx.controllerId();
        Card sourceCard = ctx.sourceCard();
        var effect = ctx.effect();
        UUID sourcePermanentId = ctx.sourcePermanentId();
        List<String> optionLabels = effect.options().stream()
                .map(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption::label)
                .filter(label -> !ctx.chosenLabels().contains(label))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        if (effect.variableModeCount() && ctx.chosenLabels().size() >= effect.choicesRequired()) {
            optionLabels.add(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.FINISH_MODE_SELECTION);
        }
        List<String> disabledLabels = effect.options().stream()
                .filter(option -> optionLabels.contains(option.label()))
                .filter(option -> option.effects().stream().anyMatch(
                        e -> e instanceof RemoveCounterFromSourceEffect removal
                                && removal.subject() instanceof PermanentSubject.Source
                                && !canRemoveCountersFromSource(gameData, sourcePermanentId, removal)))
                .map(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption::label)
                .toList();
        String prompt = effect.variableModeCount()
                ? effect.choicesRequired() == 0
                ? sourceCard.getName() + " - Choose up to " + effect.choicesMax() + " modes, or Done."
                : sourceCard.getName() + " - Choose one or more modes, or Done."
                : effect.choicesRequired() > 1
                ? sourceCard.getName() + " - Choose " + effect.choicesRequired() + " modes."
                : sourceCard.getName() + " - Choose one.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, optionLabels, prompt, disabledLabels));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a mode for {}", gameData.id, playerName, sourceCard.getName());
    }

    /**
     * Whether a mode that removes counters from its source has counters to remove. A source that is
     * gone or has too few counters greys the mode out; a dynamic amount is left available.
     */
    private boolean canRemoveCountersFromSource(GameData gameData, UUID sourcePermanentId,
                                                RemoveCounterFromSourceEffect removal) {
        if (removal.dynamicAmount() != null || sourcePermanentId == null) {
            return true;
        }
        var source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        return source != null && PermanentCounterSupport.hasCountersToRemove(
                source, removal.counterType(), removal.amount());
    }

    public void beginChooseModeOnEnterChoice(GameData gameData, UUID controllerId, Card sourceCard,
            UUID sourcePermanentId, List<String> modes) {
        beginChooseModeOnEnterChoice(gameData, controllerId, sourceCard, sourcePermanentId,
                modes, 1, List.of());
    }

    public void beginChooseModeOnEnterChoice(GameData gameData, UUID controllerId, Card sourceCard,
            UUID sourcePermanentId, List<String> modes, int choicesRequired, List<String> chosenLabels) {
        com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect =
                new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect(modes.stream()
                        .map(mode -> new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption(
                                mode, List.of()))
                        .toList(), false, choicesRequired, choicesRequired, false);
        ChoiceContext.ChooseModeChoice ctx = new ChoiceContext.ChooseModeChoice(
                sourceCard, controllerId, effect, sourcePermanentId, false, chosenLabels, true);
        List<String> availableModes = modes.stream()
                .filter(mode -> !ctx.chosenLabels().contains(mode))
                .toList();
        String prompt = choicesRequired > 1
                ? sourceCard.getName() + " - Choose " + choicesRequired + " modes."
                : sourceCard.getName() + " - Choose one.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, sourcePermanentId, null, ctx, availableModes, prompt));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose an as-enters mode for {}", gameData.id, playerName,
                sourceCard.getName());
    }

    public void beginChooseIndependentModesOnEnterChoice(GameData gameData, UUID controllerId,
            Card sourceCard, UUID sourcePermanentId, List<List<String>> modeGroups, int groupIndex) {
        if (groupIndex < 0 || groupIndex >= modeGroups.size()) {
            throw new IllegalArgumentException("Invalid as-enters mode group: " + groupIndex);
        }
        Permanent source = findPermanentById(gameData, sourcePermanentId);
        List<String> chosenLabels = source == null ? List.of() : source.getChosenModeLabels().stream().toList();
        List<String> modes = modeGroups.get(groupIndex);
        com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect =
                new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect(modes.stream()
                        .map(mode -> new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption(
                                mode, List.of()))
                        .toList(), false, 1, 1, false);
        ChoiceContext.ChooseModeChoice ctx = new ChoiceContext.ChooseModeChoice(
                sourceCard, controllerId, effect, sourcePermanentId, false, chosenLabels, true);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, sourcePermanentId, null, ctx, modes,
                sourceCard.getName() + " - Choose one."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose as-enters mode group {} for {}", gameData.id,
                playerName, groupIndex + 1, sourceCard.getName());
    }

    /** Begins the next active-player-ordered choice for an entering permanent. */
    public boolean beginChooseModeOnEnterChoiceForEachPlayer(GameData gameData, Card sourceCard,
            UUID sourcePermanentId, List<String> modes) {
        Permanent source = findPermanentById(gameData, sourcePermanentId);
        if (source == null) return false;

        UUID nextPlayer = apnapPlayerOrder(gameData).stream()
                .filter(gameData.playerIds::contains)
                .filter(playerId -> !source.getChosenModeByPlayer().containsKey(playerId))
                .findFirst()
                .orElse(null);
        if (nextPlayer == null) return false;

        beginChooseModeOnEnterChoice(gameData, nextPlayer, sourceCard, sourcePermanentId, modes);
        return true;
    }

    private static Permanent findPermanentById(GameData gameData, UUID permanentId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(permanentId)) return permanent;
            }
        }
        return null;
    }

    private static List<UUID> apnapPlayerOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) return ordered;
        List<UUID> rotated = new ArrayList<>(ordered.size());
        rotated.addAll(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }

    public void beginLibraryCastModeChoice(GameData gameData, UUID controllerId, Card cardToCast,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, StackEntryType spellType,
            List<Integer> modeIndices) {
        beginLibraryCastModeChoice(gameData, controllerId, cardToCast, effect, spellType, modeIndices, null);
    }

    public void beginLibraryCastModeChoice(GameData gameData, UUID controllerId, Card cardToCast,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, StackEntryType spellType,
            List<Integer> modeIndices, Integer discoverValue) {
        beginLibraryCastModeChoice(gameData, controllerId, cardToCast, effect, spellType, modeIndices,
                discoverValue, Zone.LIBRARY);
    }

    public void beginLibraryCastModeChoice(GameData gameData, UUID controllerId, Card cardToCast,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, StackEntryType spellType,
            List<Integer> modeIndices, Integer discoverValue, Zone sourceZone) {
        ChoiceContext.LibraryCastModeChoice ctx = new ChoiceContext.LibraryCastModeChoice(
                cardToCast, controllerId, effect, spellType, modeIndices, discoverValue, sourceZone);
        List<String> optionLabels = effect.options().stream()
                .map(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption::label)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, optionLabels, cardToCast.getName() + " — Choose one."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a cast mode for {}", gameData.id, playerName,
                cardToCast.getName());
    }

    public void beginExileFreeCastModeChoice(GameData gameData, UUID controllerId, Card cardToCast,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, StackEntryType spellType,
            List<Integer> chosenModeIndices, List<Integer> offeredModeIndices,
            int maximumChoices, boolean copy) {
        beginExileFreeCastModeChoice(gameData, controllerId, cardToCast, effect, spellType,
                chosenModeIndices, offeredModeIndices, maximumChoices, copy, false);
    }

    public void beginExileFreeCastModeChoice(GameData gameData, UUID controllerId, Card cardToCast,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, StackEntryType spellType,
            List<Integer> chosenModeIndices, List<Integer> offeredModeIndices,
            int maximumChoices, boolean copy, boolean payManaCost) {
        ChoiceContext.ExileFreeCastModeChoice ctx = new ChoiceContext.ExileFreeCastModeChoice(
                cardToCast, controllerId, effect, spellType, chosenModeIndices,
                offeredModeIndices, maximumChoices, copy, payManaCost);
        List<String> optionLabels = new java.util.ArrayList<>(offeredModeIndices.stream()
                .map(effect.options()::get)
                .map(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption::label)
                .toList());
        if (chosenModeIndices.size() >= effect.choicesRequired()) {
            optionLabels.add(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.FINISH_MODE_SELECTION);
        }
        String prompt = maximumChoices > 1
                ? cardToCast.getName() + " - Choose modes, or Done."
                : cardToCast.getName() + " - Choose one.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, optionLabels, prompt));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a free-cast mode for {}", gameData.id, playerName,
                cardToCast.getName());
    }

    /**
     * Asks {@code controllerId} for the modes of a triggered ability as it goes on the stack (CR 603.3c).
     * {@code triggerRecord} is the ability the stack door parked for its mode, which the chosen modes finish.
     */
    public void beginTriggeredModalChoice(GameData gameData, UUID controllerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.ChooseOneEffect effect, UUID sourcePermanentId,
            boolean modesResetEachTurn, boolean consumeModes,
            List<com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption> chosenModes,
            UUID triggeringCardId, UUID attackedTargetId, UUID triggeringPermanentId,
            boolean rememberLastChosenMode, StackEntry triggerRecord) {
        ChoiceContext.TriggeredModalChoice ctx =
                new ChoiceContext.TriggeredModalChoice(
                        sourceCard, controllerId, effect, sourcePermanentId, modesResetEachTurn,
                        consumeModes, chosenModes, triggeringCardId, attackedTargetId, triggeringPermanentId,
                        rememberLastChosenMode, triggerRecord);
        List<String> optionLabels = new java.util.ArrayList<>(effect.options().stream()
                .filter(option -> effect.modesMayRepeat() || !chosenModes.contains(option))
                .filter(option -> triggerTargetCollector.hasLegalModeTargets(
                        gameData, option, controllerId, sourceCard))
                .map(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption::label)
                .toList());
        if (effect.optional() && chosenModes.isEmpty()) {
            optionLabels.add(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.NO_MODE_LABEL);
        }
        if (effect.variableModeCount() && chosenModes.size() >= effect.choicesRequired()) {
            optionLabels.add(com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.FINISH_MODE_SELECTION);
        }
        String prompt = effect.variableModeCount()
                ? sourceCard.getName() + " - Choose one or more modes, or Done."
                : sourceCard.getName() + " - Choose one.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, ctx, optionLabels, prompt));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose a triggered mode for {}", gameData.id, playerName,
                sourceCard.getName());
    }

    public void beginKeywordChoice(GameData gameData, UUID playerId, UUID targetId, List<Keyword> options) {
        beginKeywordChoice(gameData, playerId, targetId, options, Duration.UNTIL_END_OF_TURN, null, null);
    }

    public void beginKeywordChoice(GameData gameData, UUID playerId, UUID targetId, List<Keyword> options,
                                   Duration duration, String sourceCardName, UUID sourcePermanentId) {
        ChoiceContext.KeywordGrantChoice choiceContext = new ChoiceContext.KeywordGrantChoice(
                targetId, options, duration, sourceCardName, sourcePermanentId);

        List<String> optionNames = options.stream().map(Keyword::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, optionNames, "Choose a keyword to grant."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a keyword", gameData.id, playerName);
    }

    public void beginPregameLegacyWordChoice(GameData gameData, UUID playerId, Card sourceCard,
                                              List<String> options) {
        ChoiceContext.LegacyWordChoice choiceContext = new ChoiceContext.LegacyWordChoice(sourceCard, options);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Choose a keyword or ability word for Legacy."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a Legacy keyword or ability word", gameData.id, playerName);
    }

    /** Asks a Secret Mission's owner to secretly choose one of its missions before the game. */
    public void beginSecretMissionChoice(GameData gameData, UUID playerId, Card sourceCard, List<String> missions) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, new ChoiceContext.SecretMissionChoice(sourceCard, missions), missions,
                "Secretly choose a mission for " + sourceCard.getName() + "."));
        log.info("Game {} - Awaiting {} to choose a secret mission", gameData.id,
                gameData.playerIdToName.get(playerId));
    }

    public void beginBasicLandwalkTypeChoice(GameData gameData, UUID playerId, UUID targetId) {
        ChoiceContext.LandwalkGrantChoice choiceContext = new ChoiceContext.LandwalkGrantChoice(targetId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext,
                List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST"),
                "Choose a basic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type for landwalk", gameData.id, playerName);
    }

    public void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId) {
        beginSubtypeChoice(gameData, playerId, permanentId, List.of());
    }

    public void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                   List<CardSubtype> allowedSubtypes) {
        beginSubtypeChoice(gameData, playerId, permanentId, allowedSubtypes, false);
    }

    public void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                   List<CardSubtype> allowedSubtypes, boolean landPlay) {
        beginSubtypeChoice(gameData, playerId, permanentId, allowedSubtypes, landPlay,
                "Choose a creature type.");
    }

    public void beginSubtypeChoice(GameData gameData, UUID controllerId, UUID permanentId,
                                   List<CardSubtype> allowedSubtypes, boolean landPlay,
                                   boolean opponentChooses) {
        if (opponentChooses) {
            beginSubtypeChoiceByOpponent(gameData, controllerId, permanentId, allowedSubtypes, landPlay);
            return;
        }
        beginSubtypeChoice(gameData, controllerId, permanentId, allowedSubtypes, landPlay,
                "Choose a creature type.");
    }

    public void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                   SubtypeChoiceOnEnterEffect choiceEffect) {
        if (choiceEffect instanceof ChooseSubtypeForSourceEffect chooseSubtype) {
            beginSubtypeChoiceForSource(gameData, playerId, permanentId, choiceEffect.allowedSubtypes(),
                    choiceEffect.choicePrompt());
            return;
        }
        if (choiceEffect instanceof ChooseSubtypeOnEnterEffect chooseSubtype && chooseSubtype.opponentChooses()) {
            beginSubtypeChoiceByOpponent(gameData, playerId, permanentId, choiceEffect.allowedSubtypes(), false);
            return;
        }
        if (choiceEffect.writesToBuddyList()) {
            Permanent entering = gameQueryService.findPermanentById(gameData, permanentId);
            List<CardSubtype> choices = entering == null ? choiceEffect.allowedSubtypes()
                    : gameQueryService.effectiveCreatureSubtypes(gameData, entering).stream().toList();
            beginSubtypeChoice(gameData, playerId, permanentId, choices, false,
                    choiceEffect.choicePrompt(), false, true);
            return;
        }
        beginSubtypeChoice(gameData, playerId, permanentId, choiceEffect.allowedSubtypes(), false,
                choiceEffect.choicePrompt());
    }

    public void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                   SubtypeChoiceOnEnterEffect choiceEffect, boolean landPlay) {
        if (choiceEffect instanceof ChooseSubtypeForSourceEffect chooseSubtype) {
            beginSubtypeChoiceForSource(gameData, playerId, permanentId, choiceEffect.allowedSubtypes(),
                    choiceEffect.choicePrompt());
            return;
        }
        if (choiceEffect instanceof ChooseSubtypeOnEnterEffect chooseSubtype && chooseSubtype.opponentChooses()) {
            beginSubtypeChoiceByOpponent(gameData, playerId, permanentId, choiceEffect.allowedSubtypes(), landPlay);
            return;
        }
        if (choiceEffect.writesToBuddyList()) {
            beginSubtypeChoice(gameData, playerId, permanentId, choiceEffect.allowedSubtypes(), landPlay,
                    choiceEffect.choicePrompt(), false, true);
            return;
        }
        beginSubtypeChoice(gameData, playerId, permanentId, choiceEffect.allowedSubtypes(), landPlay,
                choiceEffect.choicePrompt());
    }

    public void beginPregameSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                          SubtypeChoiceOnEnterEffect choiceEffect) {
        beginSubtypeChoice(gameData, playerId, permanentId, choiceEffect.allowedSubtypes(), false,
                choiceEffect.choicePrompt(), true);
    }

    private void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                    List<CardSubtype> allowedSubtypes, boolean landPlay, String prompt) {
        beginSubtypeChoice(gameData, playerId, permanentId, allowedSubtypes, landPlay, prompt, false);
    }

    private void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                    List<CardSubtype> allowedSubtypes, boolean landPlay, String prompt,
                                    boolean continueGameStart) {
        beginSubtypeChoice(gameData, playerId, permanentId, allowedSubtypes, landPlay, prompt,
                continueGameStart, false);
    }

    private void beginSubtypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                    List<CardSubtype> allowedSubtypes, boolean landPlay, String prompt,
                                    boolean continueGameStart, boolean buddyListChoice) {
        ChoiceContext.SubtypeChoice choiceContext =
                new ChoiceContext.SubtypeChoice(permanentId, landPlay, continueGameStart, buddyListChoice);

        List<CardSubtype> choices = allowedSubtypes == null || allowedSubtypes.isEmpty()
                ? Arrays.stream(CardSubtype.values())
                .filter(s -> !NON_CREATURE_SUBTYPES.contains(s))
                .toList()
                : List.copyOf(allowedSubtypes);
        if (buddyListChoice) {
            choices = choices.stream()
                    .filter(subtype -> !gameData.getBuddyList(playerId).contains(subtype))
                    .toList();
            if (choices.isEmpty()) {
                Permanent permanent = findPermanentById(gameData, permanentId);
                if (permanent != null) {
                    permanent.setBuddyListChoiceMade(true);
                    battlefieldEntryService.applyDeferredEnterWithCounters(gameData, playerId, permanent);
                    battlefieldEntryService.processCreatureETBEffects(
                            gameData, playerId, permanent.getCard(), null, true);
                }
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }
        List<String> creatureTypes = choices.stream()
                .map(CardSubtype::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, creatureTypes, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {}: {}", gameData.id, playerName, prompt);
    }

    public void beginSubtypeChoiceForSource(GameData gameData, UUID playerId, UUID permanentId,
                                            List<CardSubtype> allowedSubtypes) {
        beginSubtypeChoiceForSource(gameData, playerId, permanentId, allowedSubtypes, "Choose a creature type.");
    }

    public void beginSubtypeChoiceForSource(GameData gameData, UUID playerId, UUID permanentId,
                                            List<CardSubtype> allowedSubtypes, String prompt) {
        ChoiceContext.SourceSubtypeChoice choiceContext = new ChoiceContext.SourceSubtypeChoice(permanentId);

        List<CardSubtype> choices = allowedSubtypes == null || allowedSubtypes.isEmpty()
                ? Arrays.stream(CardSubtype.values())
                .filter(s -> !NON_CREATURE_SUBTYPES.contains(s))
                .toList()
                : allowedSubtypes;
        List<String> creatureTypes = choices.stream()
                .map(CardSubtype::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, creatureTypes, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to secretly choose a creature type", gameData.id, playerName);
    }

    private void beginSubtypeChoiceByOpponent(GameData gameData, UUID controllerId, UUID permanentId,
                                              List<CardSubtype> allowedSubtypes, boolean landPlay) {
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }
        if (opponents.size() == 1) {
            beginSubtypeChoice(gameData, opponents.getFirst(), permanentId, allowedSubtypes, landPlay);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ChooseOpponentForSubtype(
                controllerId, permanentId, allowedSubtypes, landPlay));
        beginPlayerChoice(gameData, controllerId, opponents,
                "Choose an opponent to choose a creature type.");
    }

    public void beginBuddyListChoice(GameData gameData, UUID playerId, List<CardSubtype> availableSubtypes) {
        List<String> choices = availableSubtypes.stream()
                .filter(subtype -> !gameData.getBuddyList(playerId).contains(subtype))
                .map(CardSubtype::name)
                .toList();
        if (choices.isEmpty()) {
            return;
        }
        ChoiceContext.BuddyListChoice choiceContext = new ChoiceContext.BuddyListChoice(playerId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, choices,
                "Choose a creature type for your buddy list."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a creature type for their buddy list", gameData.id, playerName);
    }

    public void beginSpellCreatureTypeChoice(GameData gameData, UUID playerId) {
        beginSpellCreatureTypeChoice(gameData, playerId, Set.of());
    }

    public void beginSpellCreatureTypeChoice(GameData gameData, UUID playerId,
                                             Set<CardSubtype> excludedSubtypes) {
        ChoiceContext.SpellCreatureTypeChoice choiceContext = new ChoiceContext.SpellCreatureTypeChoice(playerId);

        List<String> creatureTypes = Arrays.stream(CardSubtype.values())
                .filter(s -> !NON_CREATURE_SUBTYPES.contains(s) && !excludedSubtypes.contains(s))
                .map(CardSubtype::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, creatureTypes, "Choose a creature type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a creature type", gameData.id, playerName);
    }

    /** Asks {@code playerId} to choose a planeswalker type for the resolving spell or ability. */
    public void beginSpellPlaneswalkerTypeChoice(GameData gameData, UUID playerId) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, new ChoiceContext.SpellPlaneswalkerTypeChoice(playerId),
                CardSubtype.planeswalkerTypes().stream().map(CardSubtype::name).toList(),
                "Choose a planeswalker type."));
        log.info("Game {} - Awaiting {} to choose a planeswalker type", gameData.id,
                gameData.playerIdToName.get(playerId));
    }

    public void beginSpellNonbasicLandTypeChoice(GameData gameData, UUID playerId) {
        ChoiceContext.SpellNonbasicLandTypeChoice choiceContext =
                new ChoiceContext.SpellNonbasicLandTypeChoice(playerId);
        List<String> landTypes = CardSubtype.landTypes().stream()
                .filter(subtype -> !CardSubtype.basicLandTypes().contains(subtype))
                .map(CardSubtype::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, landTypes, "Choose a nonbasic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a nonbasic land type", gameData.id, playerName);
    }

    public void beginSpellCardTypeChoice(GameData gameData, UUID playerId) {
        beginSpellCardTypeChoice(gameData, playerId, List.of(CardType.values()));
    }

    public void beginSpellCardTypeChoice(GameData gameData, UUID playerId, List<CardType> allowedTypes) {
        ChoiceContext.SpellCardTypeChoice choiceContext = new ChoiceContext.SpellCardTypeChoice(playerId);
        List<String> cardTypes = allowedTypes.stream()
                .filter(type -> type != CardType.EMBLEM)
                .map(CardType::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, cardTypes, "Choose a card type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card type", gameData.id, playerName);
    }

    public void beginCardTypeOnEnterChoice(GameData gameData, UUID playerId, Card card,
                                           List<CardType> excludedTypes) {
        beginCardTypeOnEnterChoice(gameData, playerId, card, excludedTypes, null);
    }

    public void beginCardTypeOnEnterChoice(GameData gameData, UUID playerId, Card card,
                                           List<CardType> excludedTypes, Permanent preparedPermanent) {
        ChoiceContext.CardTypeOnEnterChoice choiceContext =
                new ChoiceContext.CardTypeOnEnterChoice(card, playerId, excludedTypes, preparedPermanent);
        List<String> cardTypes = Arrays.stream(CardType.values())
                .filter(type -> type != CardType.EMBLEM && !excludedTypes.contains(type))
                .map(CardType::name)
                .toList();
        String excludedLabel = excludedTypes.stream()
                .map(type -> type.getDisplayName().toLowerCase())
                .reduce((first, second) -> first + "/" + second)
                .orElse(null);
        String prompt = excludedLabel == null
                ? "Choose a card type."
                : "Choose a card type other than " + excludedLabel + ".";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, cardTypes, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card type for {}", gameData.id, playerName, card.getName());
    }

    public void beginCraftedCardTypeOnEnterChoice(GameData gameData, UUID playerId,
                                                  Permanent permanent, List<CardType> allowedTypes) {
        ChoiceContext.CraftedCardTypeOnEnterChoice choiceContext =
                new ChoiceContext.CraftedCardTypeOnEnterChoice(permanent, playerId, allowedTypes);
        List<String> cardTypes = allowedTypes.stream().map(CardType::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, cardTypes,
                "Choose a card type shared by the two craft materials."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a shared craft card type for {}", gameData.id,
                playerName, permanent.getCard().getName());
    }

    public void beginSpellColorChoice(GameData gameData, UUID playerId) {
        ChoiceContext.SpellColorChoice choiceContext = new ChoiceContext.SpellColorChoice(playerId);
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color for a spell", gameData.id, playerName);
    }

    public void beginSpellNumberChoice(GameData gameData, UUID playerId, int maxNumber) {
        beginSpellNumberChoice(gameData, playerId, 0, maxNumber);
    }

    public void beginSpellNumberChoice(GameData gameData, UUID playerId, List<Integer> choices) {
        ChoiceContext.SpellNumberChoice choiceContext = new ChoiceContext.SpellNumberChoice(playerId);
        List<String> numbers = choices.stream().distinct().map(String::valueOf).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, numbers, "Choose a number."));
        log.info("Game {} - Awaiting {} to choose a number for a spell", gameData.id,
                gameData.playerIdToName.get(playerId));
    }

    public void beginSpellNumberChoice(GameData gameData, UUID playerId, int minNumber, int maxNumber) {
        ChoiceContext.SpellNumberChoice choiceContext = new ChoiceContext.SpellNumberChoice(playerId);
        List<String> numbers = java.util.stream.IntStream.rangeClosed(minNumber, maxNumber)
                .mapToObj(Integer::toString)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, numbers,
                minNumber == 0 ? "Choose a number." : "Choose a number between " + minNumber + " and " + maxNumber + "."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a number for a spell", gameData.id, playerName);
    }

    public void beginSpellManaValueParityChoice(GameData gameData, UUID playerId) {
        ChoiceContext.SpellManaValueParityChoice choiceContext =
                new ChoiceContext.SpellManaValueParityChoice(playerId);
        List<String> options = List.of("ODD", "EVEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options, "Choose odd or even."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose odd or even for a spell", gameData.id, playerName);
    }

    public void beginManaValueParityChoice(GameData gameData, UUID playerId, UUID permanentId) {
        beginManaValueParityChoice(gameData, playerId, new ChoiceContext.ManaValueParityChoice(permanentId));
    }

    public void beginManaValueParityChoice(GameData gameData, UUID playerId,
                                           ChoiceContext.ManaValueParityChoice choiceContext) {

        List<String> options = List.of("ODD", "EVEN");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options, "Choose odd or even."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose odd or even", gameData.id, playerName);
    }

    public void beginNumberChoice(GameData gameData, UUID playerId, UUID permanentId, int min, int max) {
        ChoiceContext.NumberChoice choiceContext = new ChoiceContext.NumberChoice(permanentId);

        List<String> options = java.util.stream.IntStream.rangeClosed(min, max)
                .mapToObj(Integer::toString)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Choose a number between " + min + " and " + max + "."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a number between {} and {}", gameData.id, playerName, min, max);
    }

    public void beginPayAnyAmountOfLifeChoice(GameData gameData, UUID playerId, int maxLife,
                                              ChoiceContext.PayAnyAmountOfLifeAsEnters choiceContext) {
        List<String> options = java.util.stream.IntStream.rangeClosed(0, Math.max(0, maxLife))
                .mapToObj(Integer::toString)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                choiceContext.card().getName() + " — pay any amount of life (0-" + Math.max(0, maxLife) + ")."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to pay any amount of life for {}", gameData.id, playerName,
                choiceContext.card().getName());
    }

    public void beginTetravusCounterRemovalChoice(GameData gameData, UUID playerId, UUID permanentId,
                                                  int maxCounters,
                                                  com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate) {
        ChoiceContext.TetravusCounterRemoval choiceContext =
                new ChoiceContext.TetravusCounterRemoval(permanentId, tokenTemplate);

        List<String> options = java.util.stream.IntStream.rangeClosed(0, maxCounters)
                .mapToObj(Integer::toString)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Remove any number of +1/+1 counters (0-" + maxCounters
                        + ") to create that many Tetravite tokens."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose how many +1/+1 counters to remove (0-{})",
                gameData.id, playerName, maxCounters);
    }

    /**
     * "Move any number of counters": prompt {@code playerId} for how many counters of the asked slot's kind (0..{@code
     * max}) to move from {@code fromName} onto {@code toName}, or to remove from {@code fromName} when {@code toName} is
     * {@code null} (a removal slot). {@link ChoiceHandlerService} applies the answer through
     * {@code PermanentCounterSupport.answerCounterMove} or, for a removal, the removal step's handler.
     */
    public void beginMoveCountersAmountChoice(GameData gameData, UUID playerId,
                                              ChoiceContext.MoveCountersAmountChoice choiceContext,
                                              String fromName, String toName, String counterName, int max) {
        List<String> options = IntStream.rangeClosed(0, Math.max(0, max))
                .mapToObj(Integer::toString)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                choiceContext.sourceCardName() + " — choose how many " + counterName + " counters to "
                        + (toName == null ? "remove from " + fromName : "move from " + fromName + " onto " + toName)
                        + " (0-" + Math.max(0, max) + ")."));
        log.info("Game {} - Awaiting {} to choose how many {} counters to move (0-{})",
                gameData.id, gameData.playerIdToName.get(playerId), counterName, max);
    }

    /**
     * Quarry Hauler: prompt {@code playerId} to add or remove one counter of the FIRST kind in
     * {@code remainingKinds} on {@code targetId}. {@link ChoiceHandlerService} applies the answer and
     * re-invokes this with the remaining kinds until every kind has been resolved.
     */
    public void beginAdjustCounterKindChoice(GameData gameData, UUID playerId, UUID targetId,
                                             String sourceCardName, List<CounterType> remainingKinds) {
        ChoiceContext.AdjustCounterKindChoice ctx = new ChoiceContext.AdjustCounterKindChoice(
                targetId, playerId, sourceCardName, new ArrayList<>(remainingKinds));

        CounterType current = remainingKinds.getFirst();
        String label = current.name().toLowerCase().replace('_', ' ');
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, ctx, ChoiceContext.AdjustCounterKindChoice.OPTIONS,
                sourceCardName + " — Add or remove a " + label + " counter?"));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to add/remove a {} counter", gameData.id, playerName, current);
    }

    public void beginRemoveCountersOfKindChoice(GameData gameData, UUID playerId, UUID targetId,
                                                String sourceCardName, List<String> counterKinds,
                                                int index, int remaining, int maxForCurrentKind) {
        beginRemoveCountersOfKindChoice(gameData, playerId, targetId, sourceCardName, counterKinds,
                index, remaining, maxForCurrentKind, null, 0);
    }

    public void beginRemoveCountersOfKindChoice(GameData gameData, UUID playerId, UUID targetId,
                                                String sourceCardName, List<String> counterKinds,
                                                int index, int remaining, int maxForCurrentKind,
                                                UUID boostSourcePermanentId, int removedSoFar) {
        ChoiceContext.RemoveCountersOfKindChoice context = new ChoiceContext.RemoveCountersOfKindChoice(
                targetId, playerId, sourceCardName, counterKinds, index, remaining,
                boostSourcePermanentId, removedSoFar);
        String counterKind = counterKinds.get(index);
        int max = Math.max(0, Math.min(remaining, maxForCurrentKind));
        List<String> options = IntStream.rangeClosed(0, max).mapToObj(Integer::toString).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, options,
                sourceCardName + " — choose how many "
                        + ChoiceContext.RemoveCountersOfKindChoice.counterLabel(counterKind)
                        + " counters to remove (0-" + max + ")."));
    }

    /** Clockspinning: choose which counter on the target to add or remove. */
    public void beginAdjustChosenCounterTypeChoice(GameData gameData, UUID playerId, UUID targetId,
                                                   Zone targetZone, String sourceCardName,
                                                   List<CounterType> counterTypes) {
        ChoiceContext.AdjustChosenCounterTypeChoice context = new ChoiceContext.AdjustChosenCounterTypeChoice(
                targetId, targetZone, playerId, sourceCardName, counterTypes);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, context.options(),
                sourceCardName + " — Choose a counter to add or remove."));
        log.info("Game {} - Awaiting {} to choose a counter kind for {}", gameData.id, playerId, targetId);
    }

    /** Clockspinning: choose whether to add or remove the selected counter. */
    public void beginAdjustChosenCounterActionChoice(GameData gameData, UUID playerId, UUID targetId,
                                                     Zone targetZone, String sourceCardName,
                                                     CounterType counterType) {
        ChoiceContext.AdjustChosenCounterActionChoice context =
                new ChoiceContext.AdjustChosenCounterActionChoice(
                        targetId, targetZone, playerId, sourceCardName, counterType);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, ChoiceContext.AdjustChosenCounterActionChoice.OPTIONS,
                sourceCardName + " — Add or remove a "
                        + ChoiceContext.AdjustChosenCounterTypeChoice.counterLabel(counterType) + "?"));
        log.info("Game {} - Awaiting {} to add/remove a {} counter", gameData.id, playerId, counterType);
    }

    /** Begins one of the per-object add/remove/skip choices for time travel. */
    public void beginTimeTravelChoice(GameData gameData, ChoiceContext.TimeTravelActionChoice context) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                context.controllerId(), null, null, context,
                ChoiceContext.TimeTravelActionChoice.OPTIONS,
                context.sourceCardName() + " — Add, remove, or skip a time counter?"));
    }

    /** Animation Module: choose a counter kind already present on the target, then add one more. */
    public void beginAddAnotherCounterTypeChoice(GameData gameData, UUID playerId, UUID targetId,
                                                  String sourceCardName, List<CounterType> counterTypes,
                                                  boolean poisonCounters) {
        beginAddAnotherCounterTypeChoice(gameData, playerId, targetId, sourceCardName, counterTypes,
                poisonCounters, false);
    }

    public void beginAddAnotherCounterTypeChoice(GameData gameData, UUID playerId, UUID targetId,
                                                  String sourceCardName, List<CounterType> counterTypes,
                                                  boolean poisonCounters,
                                                  boolean distributeToOtherControlledCreatures) {
        beginAddAnotherCounterTypeChoice(gameData, playerId, targetId, sourceCardName, counterTypes,
                poisonCounters, distributeToOtherControlledCreatures, null);
    }

    public void beginAddAnotherCounterTypeChoice(GameData gameData, UUID playerId, UUID targetId,
                                                  String sourceCardName, List<CounterType> counterTypes,
                                                  boolean poisonCounters,
                                                  boolean distributeToOtherControlledCreatures,
                                                  UUID placementTargetId) {
        ChoiceContext.AddAnotherCounterTypeChoice context = new ChoiceContext.AddAnotherCounterTypeChoice(
                targetId, playerId, sourceCardName, new ArrayList<>(counterTypes), poisonCounters,
                distributeToOtherControlledCreatures, placementTargetId);
        List<String> options = context.options();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, options,
                sourceCardName + " — Choose a counter to add another of."));
        log.info("Game {} - Awaiting {} to choose a counter kind for {}", gameData.id, playerId, targetId);
    }

    /** The Caves of Androzani: choose a counter kind, or decline, for one non-Saga permanent. */
    public void beginAddAnotherCounterTypeOnEachNonSagaPermanentChoice(
            GameData gameData, UUID playerId, UUID targetId, String sourceCardName,
            List<UUID> remainingTargetIds, List<CounterType> counterTypes) {
        ChoiceContext.AddAnotherCounterTypeOnEachNonSagaPermanentChoice context =
                new ChoiceContext.AddAnotherCounterTypeOnEachNonSagaPermanentChoice(
                        targetId, playerId, sourceCardName, remainingTargetIds, counterTypes);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, context.options(),
                sourceCardName + " — Choose a counter to add another of, or skip."));
        log.info("Game {} - Awaiting {} to choose a counter kind for non-Saga permanent {}",
                gameData.id, playerId, targetId);
    }

    /** Bribe Taker: choose whether to put a +1/+1 counter or the current kind on the source. */
    public void beginChooseCounterForEachControlledCounterKindChoice(
            GameData gameData, UUID playerId, UUID sourcePermanentId, String sourceCardName,
            List<CounterType> remainingKinds) {
        ChoiceContext.ChooseCounterForEachControlledCounterKindChoice context =
                new ChoiceContext.ChooseCounterForEachControlledCounterKindChoice(
                        sourcePermanentId, playerId, sourceCardName, remainingKinds);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, context.options(),
                sourceCardName + " — Choose a counter to put on it, or decline."));
    }

    /** Invoke the Ancients: choose a keyword counter for one created token. */
    public void beginCreateTokenCounterChoice(GameData gameData, UUID playerId, Card sourceCard,
                                              List<UUID> tokenIds, List<CounterType> counterTypes) {
        ChoiceContext.CreateTokenCounterChoice context = new ChoiceContext.CreateTokenCounterChoice(
                sourceCard, playerId, tokenIds.getFirst(), tokenIds.subList(1, tokenIds.size()), counterTypes);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, context.options(),
                sourceCard.getName() + " — Choose a counter type for the token."));
        log.info("Game {} - Awaiting {} to choose a counter for created token {}",
                gameData.id, playerId, tokenIds.getFirst());
    }

    public void beginRemoveChosenCountersChoice(GameData gameData, UUID playerId, UUID targetId,
                                                 String sourceCardName, int remainingSelections,
                                                 List<CounterType> counterTypes) {
        beginRemoveChosenCountersChoice(gameData, playerId, targetId, sourceCardName,
                remainingSelections, counterTypes, false);
    }

    public void beginRemoveChosenCountersChoice(GameData gameData, UUID playerId, UUID targetId,
                                                String sourceCardName, int remainingSelections,
                                                List<CounterType> counterTypes, boolean exactAmount) {
        ChoiceContext.RemoveChosenCountersChoice context = new ChoiceContext.RemoveChosenCountersChoice(
                targetId, playerId, sourceCardName, remainingSelections, counterTypes, exactAmount);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, context.options(),
                sourceCardName + (exactAmount ? " — Choose " : " — Choose up to ")
                        + remainingSelections + " counters to remove."));
        log.info("Game {} - Awaiting {} to choose a counter to remove from {}", gameData.id, playerId, targetId);
    }

    public void beginRemoveCountersFromForcedCostOrElseChoice(GameData gameData,
            PendingMayAbility ability,
            com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect effect,
            UUID payerId, int remaining,
            Map<String, ChoiceContext.CounterSelection> permanentOptions) {
        ChoiceContext.RemoveCountersFromForcedCostOrElse context =
                new ChoiceContext.RemoveCountersFromForcedCostOrElse(
                        ability, effect, payerId, remaining, permanentOptions);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                payerId, null, null, context, context.options(),
                ability.sourceCard().getName() + " - Choose a permanent to remove a counter from ("
                        + remaining + " remaining)."));
        log.info("Game {} - Awaiting {} to choose a permanent from which to remove a counter",
                gameData.id, payerId);
    }

    public void beginRemoveOneCounterChoice(GameData gameData, UUID playerId, UUID targetId,
                                             String sourceCardName, List<CounterType> counterTypes) {
        beginRemoveOneCounterChoice(gameData, playerId, targetId, sourceCardName, counterTypes, true);
    }

    /**
     * Asks {@code playerId} which kind of counter to remove from the permanent. When
     * {@code requireControl} is false the permanent may be controlled by any player (a targeted
     * "remove a counter from target permanent" effect).
     */
    public void beginRemoveOneCounterChoice(GameData gameData, UUID playerId, UUID targetId,
                                             String sourceCardName, List<CounterType> counterTypes,
                                             boolean requireControl) {
        ChoiceContext.RemoveOneCounterChoice context = new ChoiceContext.RemoveOneCounterChoice(
                targetId, playerId, sourceCardName, counterTypes, requireControl);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, context.options(),
                sourceCardName + " — Choose a counter to remove."));
        log.info("Game {} - Awaiting {} to choose one counter to remove from {}", gameData.id, playerId, targetId);
    }

    public void beginMoveOneCounterChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                           UUID targetId, String sourceCardName,
                                           List<CounterType> counterTypes) {
        ChoiceContext.MoveOneCounterChoice context = new ChoiceContext.MoveOneCounterChoice(
                sourcePermanentId, targetId, playerId, sourceCardName, counterTypes);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, context.options(),
                sourceCardName + " — Choose a counter to move."));
        log.info("Game {} - Awaiting {} to choose one counter to move from {} to {}",
                gameData.id, playerId, sourcePermanentId, targetId);
    }

    /** Dismantle: choose whether the copied counter count becomes +1/+1 or charge counters. */
    public void beginDismantleCounterTypeChoice(GameData gameData, UUID playerId, int counterCount,
                                                String sourceCardName) {
        ChoiceContext.DismantleCounterTypeChoice context =
                new ChoiceContext.DismantleCounterTypeChoice(counterCount, sourceCardName);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, context, ChoiceContext.DismantleCounterTypeChoice.OPTIONS,
                sourceCardName + " — Choose a counter type."));

        log.info("Game {} - Awaiting {} to choose Dismantle's counter type", gameData.id, playerId);
    }

    public void beginPrimalClayFormChoice(GameData gameData, UUID playerId, UUID permanentId) {
        ChoiceContext.PrimalClayFormChoice choiceContext = new ChoiceContext.PrimalClayFormChoice(permanentId);

        List<String> options = Arrays.stream(com.github.laxika.magicalvibes.model.PrimalClayForm.values())
                .map(Enum::name)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Choose a shape: 3/3, 2/2 with flying, or 1/6 Wall with defender."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a Primal Clay shape", gameData.id, playerName);
    }

    public void beginPowerToughnessFormChoice(GameData gameData, UUID playerId, UUID permanentId,
                                              List<PowerToughnessForm> forms, boolean turnFaceUp) {
        ChoiceContext.PowerToughnessFormChoice choiceContext =
                new ChoiceContext.PowerToughnessFormChoice(permanentId, forms, turnFaceUp);
        List<String> options = forms.stream().map(PowerToughnessForm::label).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Choose a base power and toughness."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a base power and toughness", gameData.id, playerName);
    }

    public void beginPermanentTypeChoice(GameData gameData, UUID playerId, GraveyardChoiceDestination destination, String entryDescription) {
        ChoiceContext.PermanentTypeChoice choiceContext = new ChoiceContext.PermanentTypeChoice(playerId, destination, entryDescription);

        List<String> permanentTypes = List.of("ARTIFACT", "BATTLE", "CREATURE", "ENCHANTMENT", "LAND", "PLANESWALKER");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, permanentTypes, "Choose a permanent type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent type", gameData.id, playerName);
    }

    public void beginBasicLandTypeChoice(GameData gameData, UUID playerId, UUID permanentId) {
        beginBasicLandTypeChoice(gameData, playerId, permanentId, false, false);
    }

    /**
     * @param isSecondChoice   true when this is the second of two "as enters" land-type picks
     * @param chainSecondAfter true when answering the first pick should immediately begin the second
     */
    public void beginBasicLandTypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                         boolean isSecondChoice, boolean chainSecondAfter) {
        beginBasicLandTypeChoice(gameData, playerId, permanentId, isSecondChoice, chainSecondAfter, List.of());
    }

    /**
     * @param allowedTypes when non-empty, only these basic land types are offered
     *                     ("choose Island or Swamp" — Roots of Life)
     */
    public void beginBasicLandTypeChoice(GameData gameData, UUID playerId, UUID permanentId,
                                         boolean isSecondChoice, boolean chainSecondAfter,
                                         List<CardSubtype> allowedTypes) {
        ChoiceContext.BasicLandTypeChoice choiceContext =
                new ChoiceContext.BasicLandTypeChoice(permanentId, isSecondChoice, chainSecondAfter, allowedTypes);

        List<CardSubtype> offeredTypes = allowedTypes.isEmpty()
                ? CardSubtype.basicLandTypes()
                : allowedTypes;
        List<String> basicLandTypes = offeredTypes.stream().map(Enum::name).toList();
        boolean basicOnly = CardSubtype.basicLandTypes().containsAll(offeredTypes);
        String prompt = isSecondChoice
                ? "Choose the second basic land type."
                : basicOnly ? "Choose a basic land type." : "Choose a land type.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type (second={})", gameData.id, playerName, isSecondChoice);
    }

    public void beginAddBasicLandTypeChoice(GameData gameData, UUID playerId, UUID targetLandId, Duration duration) {
        beginAddBasicLandTypeChoice(gameData, playerId, targetLandId, duration, false);
    }

    public void beginAddBasicLandTypeChoice(GameData gameData, UUID playerId, UUID targetLandId, Duration duration, boolean replacing) {
        beginAddBasicLandTypeChoice(gameData, playerId, targetLandId, duration, replacing, List.of());
    }

    /**
     * Begins a target-land basic type choice, optionally restricting the offered types.
     */
    public void beginAddBasicLandTypeChoice(GameData gameData, UUID playerId, UUID targetLandId,
                                            Duration duration, boolean replacing,
                                            List<CardSubtype> allowedTypes) {
        beginAddBasicLandTypeChoice(gameData, playerId, List.of(targetLandId), duration, replacing, allowedTypes);
    }

    /** Begins one basic land type choice for several lands ("each land you control becomes that type"). */
    public void beginAddBasicLandTypeChoice(GameData gameData, UUID playerId, List<UUID> landIds,
                                            Duration duration, boolean replacing,
                                            List<CardSubtype> allowedTypes) {
        ChoiceContext.AddBasicLandTypeChoice choiceContext =
                new ChoiceContext.AddBasicLandTypeChoice(landIds, duration, replacing, allowedTypes);

        List<String> basicLandTypes = allowedTypes == null || allowedTypes.isEmpty()
                ? List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST")
                : allowedTypes.stream().map(Enum::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, "Choose a basic land type."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a basic land type to add", gameData.id, playerName);
    }

    /**
     * Prompts for the land type of a snow landwalk grant (Barbarian Guides). Only the five basic
     * land types are offered, matching every other land-type choice in the engine.
     */
    public void beginSnowLandwalkTypeChoice(GameData gameData, UUID playerId, UUID targetId) {
        ChoiceContext.SnowLandwalkGrantChoice choiceContext = new ChoiceContext.SnowLandwalkGrantChoice(targetId);

        // The land types of CR 205.3i, not the playtest-only ones.
        List<String> landTypes = CardSubtype.rulesLandTypes().stream().map(Enum::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, landTypes, "Choose a land type for snow landwalk."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a land type for snow landwalk", gameData.id, playerName);
    }

    public void beginLandwalkTypeChoice(GameData gameData, UUID playerId, UUID targetId) {
        ChoiceContext.LandwalkGrantChoice choiceContext = new ChoiceContext.LandwalkGrantChoice(targetId);
        List<String> landTypes = java.util.stream.Stream.concat(
                java.util.stream.Stream.of("PLAINSWALK", "ISLANDWALK", "SWAMPWALK", "MOUNTAINWALK", "FORESTWALK"),
                CardSubtype.landTypes().stream()
                        .filter(subtype -> !CardSubtype.basicLandTypes().contains(subtype))
                        .map(Enum::name)).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, landTypes, "Choose a land type for landwalk."));
    }

    /**
     * Vision Charm: prompt for a land type first ({@code fromType == null}); after that pick,
     * call again with the chosen {@code fromType} to prompt for the destination basic land type.
     */
    public void beginLandsOfTypeBecomeBasicTypeChoice(GameData gameData, UUID playerId) {
        beginLandsOfTypeBecomeBasicTypeChoice(gameData, playerId, null);
    }

    public void beginLandsOfTypeBecomeBasicTypeChoice(GameData gameData, UUID playerId, CardSubtype fromType) {
        ChoiceContext.LandsOfTypeBecomeBasicTypeChoice choiceContext =
                new ChoiceContext.LandsOfTypeBecomeBasicTypeChoice(playerId, fromType);

        boolean choosingFrom = fromType == null;
        List<String> basicLandTypes = (choosingFrom ? CardSubtype.landTypes() : CardSubtype.basicLandTypes())
                .stream().map(Enum::name).toList();
        String prompt = choosingFrom
                ? "Choose a land type."
                : "Choose a basic land type.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, basicLandTypes, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose {} for lands-of-type become (from={})",
                gameData.id, playerName, choosingFrom ? "a land type" : "a basic land type", fromType);
    }

    public void beginStorageMatrixUntapChoice(GameData gameData, UUID playerId) {
        ChoiceContext.StorageMatrixUntapChoice choiceContext = new ChoiceContext.StorageMatrixUntapChoice(playerId);

        List<String> options = List.of("ARTIFACT", "CREATURE", "LAND");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options, "Choose artifact, creature, or land to untap."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent type to untap (Storage Matrix)", gameData.id, playerName);
    }

    public void beginTurnaboutChoice(GameData gameData, UUID playerId) {
        ChoiceContext.TurnaboutChoice choiceContext = new ChoiceContext.TurnaboutChoice(playerId);
        List<String> options = List.of(
                "TAP_ARTIFACT", "TAP_CREATURE", "TAP_LAND",
                "UNTAP_ARTIFACT", "UNTAP_CREATURE", "UNTAP_LAND");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Choose whether to tap or untap artifacts, creatures, or lands."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose Turnabout action and permanent type", gameData.id, playerName);
    }

    /**
     * Teferi's Realm: {@code playerId} chooses artifact, creature, land, or non-Aura enchantment;
     * completing the choice phases out all nontoken permanents of that type.
     */
    public void beginTeferisRealmTypeChoice(GameData gameData, UUID playerId, Card sourceCard) {
        ChoiceContext.TeferisRealmTypeChoice choiceContext =
                new ChoiceContext.TeferisRealmTypeChoice(playerId, sourceCard);

        List<String> options = List.of("ARTIFACT", "CREATURE", "LAND", "NON_AURA_ENCHANTMENT");
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, options,
                "Choose artifact, creature, land, or non-Aura enchantment."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent type (Teferi's Realm)", gameData.id, playerName);
    }

    public void beginStaticOrbUntapChoice(GameData gameData, UUID playerId, List<UUID> candidateIds,
                                          int maxUntap,
                                          com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        List<com.github.laxika.magicalvibes.model.effect.StaticOrbEffect> restrictions =
                untapStepService == null ? List.of() : untapStepService.bindingUntapRestrictions(gameData, playerId);
        Set<UUID> pool = new java.util.LinkedHashSet<>(candidateIds);
        int cap = maxUntap;
        if (restrictions.size() > 1) {
            cap = 0;
            for (var restriction : restrictions) {
                pool.addAll(untapStepService.staticOrbUntapCandidates(gameData, playerId, restriction));
                cap += restriction.maxUntap();
            }
        }
        String prompt = "Choose up to " + cap + " permanent" + (cap == 1 ? "" : "s") + " to untap.";
        beginMultiPermanentChoice(gameData, playerId, List.copyOf(pool), cap,
                new MultiPermanentChoiceContext.StaticOrbUntap(playerId, filter, restrictions, List.of()),
                prompt);

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} permanent(s) to untap (untap lock)",
                gameData.id, playerName, cap);
    }

    private static List<Integer> allHandIndices(List<Card> hand) {
        return IntStream.range(0, hand.size()).boxed().toList();
    }

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST, CardSubtype.MOUNTAIN, CardSubtype.ISLAND,
            CardSubtype.PLAINS, CardSubtype.SWAMP, CardSubtype.DESERT,
            CardSubtype.CAVE, CardSubtype.GATE, CardSubtype.LOCUS,
            CardSubtype.AURA, CardSubtype.EQUIPMENT, CardSubtype.TREASURE,
            CardSubtype.INCUBATOR, CardSubtype.MUTAGEN,
            CardSubtype.CLUE, CardSubtype.BLOOD, CardSubtype.MAP, CardSubtype.GOLD,
            CardSubtype.LANDER, CardSubtype.FOOD, CardSubtype.POWERSTONE,
            CardSubtype.TOY, CardSubtype.SHARD, CardSubtype.VEHICLE,
            CardSubtype.SPACECRAFT, CardSubtype.PLANET, CardSubtype.BOOK,
            CardSubtype.LESSON, CardSubtype.TRAP, CardSubtype.SHRINE,
            CardSubtype.ARCANE, CardSubtype.OMEN, CardSubtype.SIEGE,
            CardSubtype.ROLE, CardSubtype.CASE, CardSubtype.RUNE,
            CardSubtype.PLAN, CardSubtype.ROOM, CardSubtype.FORTIFICATION,
            CardSubtype.CURSE, CardSubtype.CARTOUCHE, CardSubtype.SAGA,
            CardSubtype.SPHERE, CardSubtype.MIRRODIN, CardSubtype.SERRAS_REALM);

    static {
        NON_CREATURE_SUBTYPES.addAll(CardSubtype.landTypes());
        NON_CREATURE_SUBTYPES.addAll(CardSubtype.planeswalkerTypes());
    }

    public void beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes) {
        beginCardNameChoice(gameData, playerId, card, excludedTypes, false);
    }

    /**
     * Asks {@code playerId} to name a card. When {@code restrictToOpponentHands} is set the choice is
     * limited to the cards their opponents currently hold (Alhammarret, High Arbiter — "you choose the
     * name of a nonland card revealed this way").
     *
     * @return {@code false} when the restricted candidate list is empty, so no choice was started
     */
    public boolean beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes,
                                       boolean restrictToOpponentHands) {
        return beginCardNameChoice(gameData, playerId, card, excludedTypes, restrictToOpponentHands, false);
    }

    public boolean beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes,
                                       boolean restrictToOpponentHands, boolean nonbasicLandOnly) {
        return beginCardNameChoice(gameData, playerId, card, excludedTypes, restrictToOpponentHands,
                nonbasicLandOnly, null, null);
    }

    public boolean beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes,
                                       boolean restrictToOpponentHands, boolean nonbasicLandOnly,
                                       UUID attachedTo, CardType requiredType) {
        return beginCardNameChoice(gameData, playerId, card, excludedTypes, restrictToOpponentHands,
                nonbasicLandOnly, attachedTo, requiredType, null);
    }

    public boolean beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes,
                                       boolean restrictToOpponentHands, boolean nonbasicLandOnly,
                                       UUID attachedTo, CardType requiredType, Zone landPlayZone) {
        return beginCardNameChoice(gameData, playerId, card, excludedTypes, restrictToOpponentHands,
                nonbasicLandOnly, attachedTo, requiredType, landPlayZone, null);
    }

    public boolean beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes,
                                       boolean restrictToOpponentHands, boolean nonbasicLandOnly,
                                       UUID attachedTo, CardType requiredType, Zone landPlayZone,
                                       Permanent preparedPermanent) {
        ChoiceContext.CardNameChoice choiceContext =
                new ChoiceContext.CardNameChoice(card, playerId, excludedTypes, nonbasicLandOnly,
                        attachedTo, requiredType, landPlayZone, preparedPermanent, restrictToOpponentHands);

        List<String> cardNames;
        String prompt;
        if (restrictToOpponentHands) {
            cardNames = collectOpponentHandCardNames(gameData, playerId, excludedTypes, requiredType);
            if (cardNames.isEmpty()) {
                return false;
            }
            prompt = "Choose the name of a revealed card.";
        } else if (card.getStaticEffects().stream()
                .anyMatch(effect -> effect instanceof com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect choice
                        && choice.excludeBasicLandNames())) {
            cardNames = nonbasicCardNamesOfferedTo(gameData, playerId);
            prompt = "Choose a card name other than a basic land card name.";
        } else if (nonbasicLandOnly) {
            cardNames = nonbasicLandCardNamesOfferedTo(gameData, playerId);
            prompt = "Choose a nonbasic land card name.";
        } else if (requiredType != null) {
            cardNames = cardNamesOfferedTo(gameData, playerId, excludedTypes, requiredType);
            prompt = "Choose a " + requiredType.getDisplayName().toLowerCase() + " card name.";
        } else if (excludedTypes.isEmpty()) {
            cardNames = cardNamesOfferedTo(gameData, playerId);
            prompt = "Choose a card name.";
        } else {
            cardNames = cardNamesOfferedTo(gameData, playerId, excludedTypes, null);
            String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
            prompt = "Choose a non" + excludedLabel + " card name.";
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                playerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card name", gameData.id, playerName);
        return true;
    }

    public boolean beginCardNameChoice(GameData gameData, UUID playerId, Card card, List<CardType> excludedTypes,
                                       boolean restrictToOpponentHands, boolean nonbasicLandOnly, UUID attachedTo) {
        return beginCardNameChoice(gameData, playerId, card, excludedTypes, restrictToOpponentHands,
                nonbasicLandOnly, attachedTo, null);
    }

    /** "Choose a card name" as an effect resolves ({@link #cardNamesOfferedTo}). */
    public void beginCardNameAtResolutionChoice(GameData gameData, ChoiceContext.ChooseCardNameAtResolutionChoice context) {
        ChooseCardNameAtResolutionEffect choice = context.choice();
        List<String> cardNames;
        String prompt;
        if (choice.notBasicLandName()) {
            cardNames = nonbasicCardNamesOfferedTo(gameData, context.controllerId());
            prompt = "Choose a card name other than a basic land card name.";
        } else {
            cardNames = cardNamesOfferedTo(gameData, context.controllerId(), choice.excludedTypes(),
                    choice.requiredType());
            String qualifier = choice.requiredType() != null
                    ? choice.requiredType().getDisplayName().toLowerCase() + " "
                    : choice.excludedTypes().isEmpty() ? "" : "non" + choice.excludedTypes().stream()
                            .map(type -> type.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("") + " ";
            String article = !qualifier.isEmpty() && "aeiou".indexOf(qualifier.charAt(0)) >= 0 ? "an " : "a ";
            prompt = "Choose " + article + qualifier + "card name.";
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                context.controllerId(), null, null, context, cardNames, prompt));
    }

    public void beginLiarsPendulumNameChoice(GameData gameData, UUID controllerId, UUID targetPlayerId,
                                             UUID sourcePermanentId, Card sourceCard) {
        ChoiceContext.LiarsPendulumChoice choiceContext =
                new ChoiceContext.LiarsPendulumChoice(controllerId, targetPlayerId, sourcePermanentId, sourceCard, null);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, choiceContext, cardNamesOfferedTo(gameData, controllerId),
                "Choose a card name for Liar's Pendulum."));
    }

    public void beginLiarsPendulumGuessChoice(GameData gameData, ChoiceContext.LiarsPendulumChoice ctx) {
        gameData.recordPileGroupingOrGuess();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                ctx.targetPlayerId(), null, null, ctx, List.of("Yes", "No"),
                "Is a card named \"" + ctx.chosenName() + "\" in the controller's hand?"));
    }

    /** Begins The Toymaker's Trap's private controller number choice. */
    public void beginToymakersTrapNumberChoice(GameData gameData, UUID controllerId, UUID sourcePermanentId,
                                               Card sourceCard, List<Integer> availableNumbers) {
        ChoiceContext.ToymakersTrapChoice context = new ChoiceContext.ToymakersTrapChoice(
                controllerId, sourcePermanentId, sourceCard, null, 0, false);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, context,
                availableNumbers.stream().map(String::valueOf).toList(),
                sourceCard.getName() + " — Secretly choose an unused number."));
    }

    /** Begins The Toymaker's Trap's opponent guess. */
    public void beginToymakersTrapGuessChoice(GameData gameData, ChoiceContext.ToymakersTrapChoice context) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                context.opponentId(), null, null, context,
                java.util.stream.IntStream.rangeClosed(1, 5).mapToObj(String::valueOf).toList(),
                context.sourceCard().getName() + " — Guess the number."));
    }

    /** Distinct names of the cards held by {@code playerId}'s opponents, minus {@code excludedTypes}. */
    private List<String> collectOpponentHandCardNames(GameData gameData, UUID playerId, List<CardType> excludedTypes) {
        return collectOpponentHandCardNames(gameData, playerId, excludedTypes, null);
    }

    private List<String> collectOpponentHandCardNames(GameData gameData, UUID playerId,
                                                      List<CardType> excludedTypes, CardType requiredType) {
        Set<String> names = new TreeSet<>();
        for (UUID pid : gameData.playerIds) {
            if (pid.equals(playerId)) {
                continue;
            }
            gameData.playerHands.getOrDefault(pid, List.of()).stream()
                    .filter(c -> isNameCandidate(c, excludedTypes, requiredType))
                    .forEach(c -> names.addAll(c.getNames()));
        }
        return List.copyOf(names);
    }

    /**
     * Asks {@code ctx.choosingPlayerId()} for a card name other than a basic land card name
     * (Null Chamber). Called twice per resolution: once for the controller, then once for their
     * opponent with the first name carried in the context.
     */
    public void beginDualCardNameChoice(GameData gameData, ChoiceContext.DualCardNameChoice ctx) {
        List<String> cardNames = nonbasicCardNamesOfferedTo(gameData, ctx.choosingPlayerId());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                ctx.choosingPlayerId(), null, null, ctx, cardNames,
                "Choose a card name other than a basic land card name."));

        String playerName = gameData.playerIdToName.get(ctx.choosingPlayerId());
        log.info("Game {} - Awaiting {} to choose a card name for {}", gameData.id, playerName, ctx.card().getName());
    }

    /** Begins Assembly Hall's choice of a creature card name from the controller's hand. */
    public void beginAssemblyHallCreatureCardChoice(GameData gameData, UUID controllerId) {
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        List<String> creatureNames = hand.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(Card::getName)
                .distinct()
                .sorted()
                .toList();

        String playerName = gameData.playerIdToName.get(controllerId);
        if (creatureNames.isEmpty()) {
            log.info("Game {} - {} has no creature card in hand for Assembly Hall", gameData.id, playerName);
            com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, new ChoiceContext.AssemblyHallCreatureCardChoice(controllerId),
                creatureNames, "Choose a creature card in your hand to reveal."));
        log.info("Game {} - Awaiting {} to choose a creature card in hand for Assembly Hall",
                gameData.id, playerName);
    }

    /** Begins Infernal Tutor's choice of a card name from the controller's hand. */
    public void beginInfernalTutorCardChoice(GameData gameData, UUID controllerId) {
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        List<String> cardNames = hand.stream()
                .map(Card::getName)
                .distinct()
                .sorted()
                .toList();

        String playerName = gameData.playerIdToName.get(controllerId);
        if (cardNames.isEmpty()) {
            log.info("Game {} - {} has no card in hand for Infernal Tutor", gameData.id, playerName);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, new ChoiceContext.InfernalTutorCardChoice(controllerId),
                cardNames, "Choose a card in your hand to reveal."));
        log.info("Game {} - Awaiting {} to choose a card in hand for Infernal Tutor",
                gameData.id, playerName);
    }

    /**
     * Mindblaze, first prompt: the controller chooses a card name. {@link #beginRevealLibraryNumberGuessChoice}
     * follows once the name is in.
     */
    public void beginRevealLibraryNameGuessChoice(GameData gameData, UUID choosingPlayerId, UUID targetPlayerId,
                                                  List<CardType> excludedTypes, int damage, Card sourceCard) {
        ChoiceContext.RevealLibraryNameGuessChoice choiceContext =
                new ChoiceContext.RevealLibraryNameGuessChoice(targetPlayerId, choosingPlayerId, excludedTypes, damage, sourceCard);

        List<String> cardNames = cardNamesOfferedTo(gameData, choosingPlayerId, excludedTypes, null);
        String excludedLabel = excludedTypes.stream().map(t -> t.name().toLowerCase()).reduce((a, b) -> a + "/" + b).orElse("");
        String prompt = "Choose a non" + excludedLabel + " card name.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choosingPlayerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (reveal library, guess count)", gameData.id, playerName);
    }

    /**
     * Mindblaze, second prompt: the controller chooses a number greater than 0. The offered range
     * stops one above the target's library size — a larger guess can never match the number of cards in
     * that library, so it is outcome-identical to that always-losing guess.
     */
    public void beginRevealLibraryNumberGuessChoice(GameData gameData, ChoiceContext.RevealLibraryNumberGuessChoice ctx) {
        List<Card> library = gameData.playerDecks.get(ctx.targetPlayerId());
        int max = (library == null ? 0 : library.size()) + 1;

        List<String> options = java.util.stream.IntStream.rangeClosed(1, max)
                .mapToObj(Integer::toString)
                .toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                ctx.controllerId(), null, null, ctx, options,
                "Choose a number greater than 0 for \"" + ctx.chosenName() + "\"."));

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        log.info("Game {} - Awaiting {} to choose a number for \"{}\"", gameData.id, playerName, ctx.chosenName());
    }

    public void beginSphinxAmbassadorCardNameChoice(GameData gameData, UUID namingPlayerId, UUID controllerId) {
        ChoiceContext.SphinxAmbassadorNameChoice choiceContext = new ChoiceContext.SphinxAmbassadorNameChoice(namingPlayerId, controllerId);

        List<String> cardNames = cardNamesOfferedTo(gameData, namingPlayerId);
        String prompt = "Choose a card name.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                namingPlayerId, null, null, choiceContext, cardNames, prompt));

        String playerName = gameData.playerIdToName.get(namingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (Sphinx Ambassador)", gameData.id, playerName);
    }

    /** The names any "choose a card name" prompt with no quality offers {@code chooserId}. */
    public List<String> cardNamesOfferedTo(GameData gameData, UUID chooserId) {
        return cardNamesOfferedTo(gameData, chooserId, List.of(), null);
    }

    /**
     * The names of a card without {@code excludedTypes} and with {@code requiredType}, if set, that a "choose a card
     * name" prompt offers {@code chooserId}: those of every offered printing and of the cards the chooser can know,
     * so the list says nothing about another player's hand or library or a hidden face (CR 400.2, 406.3).
     */
    public List<String> cardNamesOfferedTo(GameData gameData, UUID chooserId, List<CardType> excludedTypes,
                                           CardType requiredType) {
        Set<String> names = new TreeSet<>(collectCardNamesKnownTo(gameData, chooserId,
                card -> isNameCandidate(card, excludedTypes, requiredType)));
        CatalogNames catalog = catalogNames();
        if (catalog != null) {
            names.addAll(catalog.matching(new CardNameFilter(List.copyOf(excludedTypes), requiredType)));
        }
        return new ArrayList<>(names);
    }

    public boolean isNameExcludedByType(GameData gameData, String cardName, List<CardType> excludedTypes) {
        return !collectCardNamesInGame(gameData,
                card -> card.getNames().contains(cardName) && hasExcludedType(card, excludedTypes)).isEmpty();
    }

    /**
     * The names of the face-up cards in the public zones, of what the stack shows ({@link FaceDownVisibility#showsNameOnStack}) and
     * of {@code viewerId}'s own hand and library.
     */
    private Set<String> collectCardNamesKnownTo(GameData gameData, UUID viewerId, Predicate<Card> candidate) {
        Set<String> names = new TreeSet<>();
        Consumer<Card> offer = card -> {
            if (candidate.test(card)) {
                names.addAll(card.getNames());
            }
        };
        for (UUID playerId : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                    .filter(permanent -> !permanent.isFaceDown())
                    .forEach(permanent -> offer.accept(permanent.getCard()));
            gameData.playerGraveyards.getOrDefault(playerId, List.of()).forEach(offer);
            gameData.getPlayerExiledCards(playerId).stream()
                    .filter(card -> !gameData.isExiledFaceDown(card.getId()))
                    .forEach(offer);
        }
        gameData.playerHands.getOrDefault(viewerId, List.of()).forEach(offer);
        gameData.playerDecks.getOrDefault(viewerId, List.of()).forEach(offer);
        gameData.stack.stream()
                .filter(entry -> FaceDownVisibility.showsNameOnStack(gameData, entry))
                .forEach(entry -> offer.accept(entry.getCard()));
        return names;
    }

    /** The names offered for "a card name other than a basic land card name". */
    private List<String> nonbasicCardNamesOfferedTo(GameData gameData, UUID chooserId) {
        Set<String> names = new TreeSet<>(collectCardNamesKnownTo(gameData, chooserId,
                card -> (!card.isToken() || card.isTokenCard())
                        && !(card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC))));
        CatalogNames catalog = catalogNames();
        if (catalog != null) {
            names.addAll(catalog.nonbasic());
        }
        return new ArrayList<>(names);
    }

    /** The names offered for "a nonbasic land card name" (Alpine Moon). */
    private List<String> nonbasicLandCardNamesOfferedTo(GameData gameData, UUID chooserId) {
        Set<String> names = new TreeSet<>(collectCardNamesKnownTo(gameData, chooserId,
                card -> (!card.isToken() || card.isTokenCard())
                        && card.hasType(CardType.LAND) && !card.getSupertypes().contains(CardSupertype.BASIC)));
        CatalogNames catalog = catalogNames();
        if (catalog != null) {
            names.addAll(catalog.nonbasicLands());
        }
        return new ArrayList<>(names);
    }

    /**
     * The name lists of the printings a name prompt offers, or null when there is no catalog to
     * offer from. They are the {@link CardNameCatalog}'s printings when there is one, every
     * implemented printing otherwise, and are rebuilt when the {@link CardNameCatalog} offers a
     * different list.
     */
    private CatalogNames catalogNames() {
        List<CardPrinting> printings;
        if (cardNameCatalog != null) {
            printings = cardNameCatalog.printings();
        } else if (cardCatalog != null) {
            printings = implementedPrintings();
        } else {
            return null;
        }
        CatalogNames current = catalogNames;
        if (current == null || current.printings != printings) {
            current = new CatalogNames(printings);
            catalogNames = current;
        }
        return current;
    }

    private List<CardPrinting> implementedPrintings() {
        List<CardPrinting> cached = implementedPrintings;
        if (cached == null) {
            List<CardPrinting> all = new ArrayList<>();
            for (CardSet set : CardSet.values()) {
                all.addAll(cardCatalog.getPrintings(set));
            }
            cached = List.copyOf(all);
            implementedPrintings = cached;
        }
        return cached;
    }

    /** Every distinct card name across all zones (battlefield, hand, graveyard, library, exile, stack)
     *  whose card satisfies {@code candidate}, sorted alphabetically. */
    private List<String> collectCardNamesInGame(GameData gameData, Predicate<Card> candidate) {
        Set<String> names = new TreeSet<>();
        for (UUID pid : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(pid, List.of()).stream()
                    .filter(p -> candidate.test(p.getCard()))
                    .forEach(p -> names.addAll(p.getCard().getNames()));
            gameData.playerHands.getOrDefault(pid, List.of()).stream()
                    .filter(candidate)
                    .forEach(c -> names.addAll(c.getNames()));
            gameData.playerGraveyards.getOrDefault(pid, List.of()).stream()
                    .filter(candidate)
                    .forEach(c -> names.addAll(c.getNames()));
            gameData.playerDecks.getOrDefault(pid, List.of()).stream()
                    .filter(candidate)
                    .forEach(c -> names.addAll(c.getNames()));
            gameData.getPlayerExiledCards(pid).stream()
                    .filter(candidate)
                    .forEach(c -> names.addAll(c.getNames()));
        }
        gameData.stack.stream()
                .filter(se -> candidate.test(se.getCard()))
                .forEach(se -> names.addAll(se.getCard().getNames()));
        return new ArrayList<>(names);
    }

    /**
     * A card's name is offered only when it has none of {@code excludedTypes} and, if set, has {@code requiredType}.
     * A token's name is not a card name unless the token is also a card, so battlefield tokens are never offered.
     */
    private boolean isNameCandidate(Card card, List<CardType> excludedTypes, CardType requiredType) {
        return (!card.isToken() || card.isTokenCard()) && !hasExcludedType(card, excludedTypes) && (requiredType == null || card.hasType(requiredType));
    }

    private boolean hasExcludedType(Card card, List<CardType> excludedTypes) {
        if (card.getType() != null && excludedTypes.contains(card.getType())) {
            return true;
        }
        for (CardType excluded : excludedTypes) {
            if (card.getAdditionalTypes().contains(excluded)) {
                return true;
            }
        }
        return false;
    }

    /**
     * "Exile any number of cards named X" from that player's hand, graveyard and library, who then shuffles;
     * {@code followUpEffect} follows the exile.
     */
    public void beginMultiZoneExileChoice(GameData gameData, UUID choosingPlayerId, List<Card> matchingCards,
                                          UUID targetPlayerId, String cardName, CardEffect followUpEffect) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiZoneExileChoice(
                choosingPlayerId, matchingCards.stream().map(Card::getId).toList(), matchingCards.size(),
                targetPlayerId, choosingPlayerId, cardName, null, followUpEffect, false, SearchedZones.NONE, true,
                false));
    }

    /** "Choose a [filter] card you own from outside the game": it stays there, and {@code followUpEffect} reads it. */
    public void beginOutsideGameCardChoice(GameData gameData, UUID playerId, List<Card> matchingCards,
                                           CardPredicate filter, String cardLabel, CardEffect followUpEffect) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SearchOutsideGameOrExileCardChoice(
                playerId, matchingCards.stream().map(Card::getId).toList(), filter, cardLabel, followUpEffect));
    }

    /** "You may exile a card from your hand or graveyard": linked to the source, and nothing is shuffled. */
    public void beginHandOrGraveyardExileChoice(GameData gameData, UUID choosingPlayerId, List<Card> matchingCards,
                                                int maxCount, UUID targetPlayerId, String cardName,
                                                UUID sourcePermanentId, CardEffect followUpEffect) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiZoneExileChoice(
                choosingPlayerId, matchingCards.stream().map(Card::getId).toList(),
                Math.min(maxCount, matchingCards.size()), targetPlayerId, choosingPlayerId, cardName,
                sourcePermanentId, followUpEffect, false, SearchedZones.NONE, false, false));
    }

    /**
     * "Search ... for cards with the same name and exile them": at most {@code maxCount} of {@code matchingCards},
     * added to the resolving step as they are exiled. The searcher is shown what is in {@code searchedZones} and
     * nothing else (CR 701.23a); {@code shufflesLibrary} is independent of it. With {@code allInGraveyard} those in
     * the graveyard are found whether chosen or not (CR 701.23b).
     */
    public void beginSameNameExileChoice(GameData gameData, UUID choosingPlayerId, List<Card> matchingCards,
                                         int maxCount, UUID searchedPlayerId, String cardNames, UUID sourcePermanentId,
                                         SearchedZones searchedZones, boolean shufflesLibrary,
                                         boolean allInGraveyard) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiZoneExileChoice(
                choosingPlayerId, matchingCards.stream().map(Card::getId).toList(),
                Math.min(maxCount, matchingCards.size()), searchedPlayerId, choosingPlayerId, cardNames,
                sourcePermanentId, null,
                allInGraveyard, searchedZones, shufflesLibrary, true));
    }

    /**
     * Begin a mixed battlefield + hand exile selection (Descent into Madness). The interaction
     * carries its own APNAP queue state, so the effect handler builds it and passes it whole.
     */
    public void beginExilePermanentsOrHandCardsChoice(GameData gameData,
            PendingInteraction.ExilePermanentsOrHandCardsChoice interaction) {
        interactionHandlerRegistry.begin(gameData, interaction);
    }

    public void beginArtifactPermanentOrGraveyardChoice(GameData gameData,
            PendingInteraction.ArtifactPermanentOrGraveyardCardChoice interaction) {
        interactionHandlerRegistry.begin(gameData, interaction);
    }

    /**
     * Begin the mixed battlefield + graveyard + hand Aura selection of Bruna, Light of Alabaster.
     * The candidate list is built by the effect handler, so the interaction arrives whole.
     */
    public void beginAttachAurasChoice(GameData gameData, PendingInteraction.AttachAurasChoice interaction) {
        interactionHandlerRegistry.begin(gameData, interaction);
    }

    public void beginReturnAurasFromGraveyardChoice(
            GameData gameData, PendingInteraction.ReturnAurasFromGraveyardChoice interaction) {
        interactionHandlerRegistry.begin(gameData, interaction);
    }

    public void beginImprintFromHandChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID sourcePermanentId) {
        beginImprintFromHandChoice(gameData, playerId, validIndices, prompt, sourcePermanentId, false);
    }

    public void beginImprintFromHandChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                           UUID sourcePermanentId, boolean grantCastPermission) {
        beginImprintFromHandChoice(gameData, playerId, validIndices, prompt, sourcePermanentId,
                grantCastPermission, false);
    }

    public void beginImprintFromHandChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                           UUID sourcePermanentId, boolean grantCastPermission, boolean faceDown) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ImprintFromHandChoice(
                playerId, new ArrayList<>(validIndices), sourcePermanentId, prompt, grantCastPermission, faceDown));
    }

    public void beginPlotFromHandChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                playerId, playerId, new ArrayList<>(validIndices), 1, false, true,
                List.of(), null, prompt, false, true, false,
                0, null, false, false, false,
                false, false, true));
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId, int remainingCount) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, null, remainingCount);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId,
                                         CardEffect chosenCardThenEffect) {
        beginExileFromHandChoice(gameData, playerId, null, null, 1,
                List.of(), 0, false, false, null, false, null, 0, false,
                chosenCardThenEffect);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, playPermissionControllerId,
                remainingCount, List.of(), 0, false);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount,
                                         List<UUID> remainingChoosers, int cardsPerPlayer) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, playPermissionControllerId,
                remainingCount, remainingChoosers, cardsPerPlayer, false, false);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount,
                                         List<UUID> remainingChoosers, int cardsPerPlayer,
                                         boolean faceDown) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, playPermissionControllerId,
                remainingCount, remainingChoosers, cardsPerPlayer, faceDown, false);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount,
                                         List<UUID> remainingChoosers, int cardsPerPlayer,
                                         boolean faceDown, boolean returnOnSourceLeave) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, playPermissionControllerId,
                remainingCount, remainingChoosers, cardsPerPlayer, faceDown, returnOnSourceLeave, null);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount,
                                         List<UUID> remainingChoosers, int cardsPerPlayer,
                                         boolean faceDown, boolean returnOnSourceLeave,
                                         UUID untapPermanentId) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, playPermissionControllerId,
                remainingCount, remainingChoosers, cardsPerPlayer, faceDown, returnOnSourceLeave,
                untapPermanentId, false, null, 0, false);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount,
                                         List<UUID> remainingChoosers, int cardsPerPlayer,
                                         boolean faceDown, boolean returnOnSourceLeave,
                                         UUID untapPermanentId, boolean playPermissionToChooser,
                                         UUID playPermissionTaxSourceControllerId,
                                         int exilePlayOpponentTax, boolean landsEnterTapped) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, playPermissionControllerId,
                remainingCount, remainingChoosers, cardsPerPlayer, faceDown, returnOnSourceLeave,
                untapPermanentId, playPermissionToChooser, playPermissionTaxSourceControllerId,
                exilePlayOpponentTax, landsEnterTapped, null);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount,
                                         List<UUID> remainingChoosers, int cardsPerPlayer,
                                         boolean faceDown, boolean returnOnSourceLeave,
                                         UUID untapPermanentId, boolean playPermissionToChooser,
                                         UUID playPermissionTaxSourceControllerId,
                                         int exilePlayOpponentTax, boolean landsEnterTapped,
                                         CardEffect chosenCardThenEffect) {
        beginExileFromHandChoice(gameData, playerId, sourcePermanentId, playPermissionControllerId,
                remainingCount, remainingChoosers, cardsPerPlayer, faceDown, returnOnSourceLeave,
                untapPermanentId, playPermissionToChooser, playPermissionTaxSourceControllerId,
                exilePlayOpponentTax, landsEnterTapped, chosenCardThenEffect, 0);
    }

    public void beginExileFromHandChoice(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                         UUID playPermissionControllerId, int remainingCount,
                                         List<UUID> remainingChoosers, int cardsPerPlayer,
                                         boolean faceDown, boolean returnOnSourceLeave,
                                         UUID untapPermanentId, boolean playPermissionToChooser,
                                         UUID playPermissionTaxSourceControllerId,
                                         int exilePlayOpponentTax, boolean landsEnterTapped,
                                         CardEffect chosenCardThenEffect, int exiledCount) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = allHandIndices(hand);

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExileFromHandChoice(
                playerId, validIndices, sourcePermanentId, playPermissionControllerId, remainingCount,
                "Choose a card to exile.", remainingChoosers != null ? remainingChoosers : List.of(),
                cardsPerPlayer, faceDown, returnOnSourceLeave, untapPermanentId,
                playPermissionToChooser, playPermissionTaxSourceControllerId,
                exilePlayOpponentTax, landsEnterTapped, chosenCardThenEffect, exiledCount));
    }

    /** "Exile a [filter] card from your hand": one of {@code validIndices}. */
    public void beginExileOneCardFromHandChoice(GameData gameData, UUID playerId, List<Integer> validIndices,
                                                boolean faceDown) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExileFromHandChoice(
                playerId, List.copyOf(validIndices), null, null, 1, "Choose a card to exile.", List.of(), 0,
                faceDown, false, null, false, null, 0, false, null, 0));
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount) {
        beginDiscardChoice(gameData, playerId, remainingCount, DiscardFollowUp.NONE);
    }

    public void beginPutCardFromHandIntoGraveyardChoice(GameData gameData, UUID playerId,
                                                        List<Integer> validIndices, String prompt) {
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PutCardFromHandIntoGraveyardChoice(playerId, validIndices, prompt));
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount, DiscardFollowUp followUp) {
        List<Card> hand = gameData.playerHands.get(playerId);
        beginDiscardChoice(gameData, playerId, allHandIndices(hand), "Choose a card to discard.", remainingCount,
                followUp, (CardType) null, false);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount,
                                   DiscardFollowUp followUp, CardType stopAfterDiscardingType) {
        beginDiscardChoice(gameData, playerId, remainingCount, followUp, stopAfterDiscardingType, false);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount,
                                   DiscardFollowUp followUp, CardPredicate stopAfterDiscardingPredicate) {
        List<Card> hand = gameData.playerHands.get(playerId);
        beginDiscardChoice(gameData, playerId, allHandIndices(hand), "Choose a card to discard.", remainingCount,
                followUp, null, stopAfterDiscardingPredicate, false);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, int remainingCount,
                                   DiscardFollowUp followUp, CardType stopAfterDiscardingType,
                                   boolean declinable) {
        List<Card> hand = gameData.playerHands.get(playerId);
        beginDiscardChoice(gameData, playerId, allHandIndices(hand), "Choose a card to discard.", remainingCount,
                followUp, stopAfterDiscardingType, declinable);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, int remainingCount) {
        beginDiscardChoice(gameData, playerId, validIndices, prompt, remainingCount, DiscardFollowUp.NONE);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, int remainingCount, DiscardFollowUp followUp) {
        beginDiscardChoice(gameData, playerId, validIndices, prompt, remainingCount, followUp,
                (CardType) null, false);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                   int remainingCount, DiscardFollowUp followUp,
                                   CardType stopAfterDiscardingType, boolean declinable) {
        beginDiscardChoice(gameData, playerId, validIndices, prompt, remainingCount, followUp,
                stopAfterDiscardingType, null, declinable);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                   int remainingCount, DiscardFollowUp followUp,
                                   CardPredicate stopAfterDiscardingPredicate, boolean declinable) {
        beginDiscardChoice(gameData, playerId, validIndices, prompt, remainingCount, followUp,
                (CardType) null, stopAfterDiscardingPredicate, declinable);
    }

    private void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt,
                                    int remainingCount, DiscardFollowUp followUp,
                                    CardType stopAfterDiscardingType,
                                    CardPredicate stopAfterDiscardingPredicate, boolean declinable) {
        if (remainingCount > 0 && !validIndices.isEmpty()
                && !followUp.targetOpponentsDiscardThenDraw()
                && !followUp.eachPlayerQueue()
                && !followUp.plaguecrafter()
                && !playerId.equals(gameData.deferredDiscardChooserId)
                && !(gameData.eachPlayerRummage.active && gameData.eachPlayerRummage.deferDiscards)) {
            gameData.beginDiscardEvent(playerId);
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.DiscardChoice(
                playerId, new ArrayList<>(validIndices), remainingCount, followUp, prompt,
                stopAfterDiscardingType, stopAfterDiscardingPredicate, declinable));
    }

    public void processNextMayAbility(GameData gameData) {
        if (gameData.pendingMayAbilities.isEmpty()) {
            return;
        }
        if (gameData.status == GameStatus.FINISHED) {
            gameData.pendingMayAbilities.clear();
            return;
        }

        PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MayAbilityChoice(
                next.choicePlayerId() != null ? next.choicePlayerId() : next.controllerId(),
                next.description(), next.manaCost()));
    }
}
