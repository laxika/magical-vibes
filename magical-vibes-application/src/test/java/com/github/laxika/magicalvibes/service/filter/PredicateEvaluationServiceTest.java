package com.github.laxika.magicalvibes.service.filter;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyTwoColorsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasAdventurePredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsDoubleFacedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanSourceLoyaltyPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNameInControllerGraveyardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerToughnessTotalAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentActivatedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedBySourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedBySourceThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerPoisonCountersAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasNonManaActivatedAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTapActivatedAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongAllCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongAllArtifactsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLowestManaValueAmongAllNonlandPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostControllerGraveyardCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostSourceControllerHandSizePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMonocoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasExactlyTwoColorsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSuspectedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTransformedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanControllerGraveyardCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesMostCommonColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySupertypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PredicateEvaluationService} — the single evaluation point for the
 * sealed card/permanent/stack-entry predicate and target-filter hierarchies. Moved from
 * {@code GameQueryServiceTest} when predicate evaluation was extracted out of
 * {@link GameQueryService}.
 */
@ExtendWith(MockitoExtension.class)
class PredicateEvaluationServiceTest {

    @Mock
    private StaticEffectHandlerRegistry staticEffectRegistry;

    private GameQueryService gqs;
    private PredicateEvaluationService evaluator;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        gqs = new GameQueryService(staticEffectRegistry);
        evaluator = new PredicateEvaluationService(gqs);
        ReflectionTestUtils.setField(gqs, "predicateEvaluationService", evaluator);
        LayerSystemService layerSystemService = new LayerSystemService();
        ReflectionTestUtils.setField(layerSystemService, "predicateEvaluationService", evaluator);
        ReflectionTestUtils.setField(layerSystemService, "staticEffectRegistry", staticEffectRegistry);
        ReflectionTestUtils.setField(layerSystemService, "gameQueryService", gqs);
        ReflectionTestUtils.setField(gqs, "layerSystemService", layerSystemService);

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ===== Helper methods =====

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setColors(color == null ? List.of() : List.of(color));
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createCreatureWithSubtypes(String name, int power, int toughness, CardColor color, List<CardSubtype> subtypes) {
        Card card = createCreature(name, power, toughness, color);
        card.setSubtypes(subtypes);
        return card;
    }

    private static Card createArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{1}");
        return card;
    }

    private static Card createPlaneswalker(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.PLANESWALKER);
        card.setManaCost(manaCost);
        card.setLoyalty(3);
        return card;
    }

    private static Card createBattle(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.BATTLE);
        card.setManaCost("{1}");
        return card;
    }

    private static Card createArtifactCreature(String name, int power, int toughness, List<CardSubtype> subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(EnumSet.of(CardType.ARTIFACT));
        card.setManaCost("{1}");
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(subtypes);
        return card;
    }

    private static Card createLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        return card;
    }

    private static Card createEnchantment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        return card;
    }

    private static Card createAura(String name, CardEffect staticEffect) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.AURA));
        card.addEffect(EffectSlot.STATIC, staticEffect);
        return card;
    }

    private static Card createEnchantmentWithStaticEffect(String name, CardEffect effect) {
        Card card = createEnchantment(name);
        card.addEffect(EffectSlot.STATIC, effect);
        return card;
    }

    private static Card createMirranCrusader() {
        Card card = createCreature("Mirran Crusader", 2, 2, CardColor.WHITE);
        card.setKeywords(EnumSet.of(Keyword.DOUBLE_STRIKE));
        card.addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(EnumSet.of(CardColor.BLACK, CardColor.GREEN)));
        return card;
    }

    private static Card createChangelingCreature(String name) {
        Card card = createCreature(name, 2, 2, CardColor.GREEN);
        card.setSubtypes(List.of(CardSubtype.SHAPESHIFTER));
        card.setKeywords(EnumSet.of(Keyword.CHANGELING));
        return card;
    }

    private Permanent addPermanent(UUID playerId, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    // ===== matchesCardPredicate =====

    @Nested
    @DisplayName("matchesCardPredicate")
    class MatchesCardPredicate {

        @Test
        void matchesCardsWithAdventureCastingOption() {
            Card adventure = new Card();
            adventure.addCastingOption(new AdventureCast("{1}{G}"));
            Card ordinary = new Card();

            assertThat(evaluator.matchesCardPredicate(adventure, new CardHasAdventurePredicate(), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(ordinary, new CardHasAdventurePredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("null predicate returns true")
        void nullPredicateReturnsTrue() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(card, null, null)).isTrue();
        }

        @Test
        @DisplayName("CardManaValueLessThanSourceLoyaltyPredicate uses the source permanent's loyalty")
        void cardManaValueLessThanSourceLoyaltyPredicateMatches() {
            Card sourceCard = new Card();
            Permanent source = addPermanent(player1Id, sourceCard);
            source.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 3);

            Card eligible = createCreature("Eligible", 2, 2, CardColor.GREEN);
            eligible.setManaCost("{2}");
            Card tooExpensive = createCreature("Too Expensive", 2, 2, CardColor.GREEN);
            tooExpensive.setManaCost("{3}");
            CardManaValueLessThanSourceLoyaltyPredicate predicate =
                    new CardManaValueLessThanSourceLoyaltyPredicate();

            assertThat(evaluator.matchesCardPredicate(eligible, predicate, sourceCard.getId(), gd, player1Id))
                    .isTrue();
            assertThat(evaluator.matchesCardPredicate(tooExpensive, predicate, sourceCard.getId(), gd, player1Id))
                    .isFalse();
        }

        @Test
        @DisplayName("CardManaValueLessThanSourcePowerPredicate is strict")
        void cardManaValueLessThanSourcePowerPredicateIsStrict() {
            Card sourceCard = createCreature("Narset", 3, 4, CardColor.WHITE);
            addPermanent(player1Id, sourceCard);

            Card belowPower = createCreature("Below Power", 2, 2, CardColor.BLUE);
            belowPower.setManaCost("{2}");
            Card equalToPower = createCreature("Equal to Power", 3, 3, CardColor.BLUE);
            equalToPower.setManaCost("{3}");
            CardManaValueLessThanSourcePowerPredicate predicate =
                    new CardManaValueLessThanSourcePowerPredicate();

            assertThat(evaluator.matchesCardPredicate(belowPower, predicate, sourceCard.getId(), gd, player1Id))
                    .isTrue();
            assertThat(evaluator.matchesCardPredicate(equalToPower, predicate, sourceCard.getId(), gd, player1Id))
                    .isFalse();
        }

        @Test
        @DisplayName("CardTruePredicate always returns true")
        void cardTruePredicateAlwaysMatches() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(card, new CardTruePredicate(), null)).isTrue();
        }

        @Test
        @DisplayName("CardTypePredicate matches primary type")
        void cardTypePredicateMatchesPrimaryType() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(card, new CardTypePredicate(CardType.CREATURE), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(card, new CardTypePredicate(CardType.ARTIFACT), null)).isFalse();
        }

        @Test
        @DisplayName("CardTypePredicate matches additional type")
        void cardTypePredicateMatchesAdditionalType() {
            Card card = createArtifactCreature("Myr Sire", 1, 1, List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR));

            assertThat(evaluator.matchesCardPredicate(card, new CardTypePredicate(CardType.ARTIFACT), null)).isTrue();
        }

        @Test
        @DisplayName("CardSharesCardTypeWithImprintedCardPredicate matches broadly before a cost is paid")
        void cardTypeSharingPredicateMatchesBeforeCostImprintsCard() {
            Card source = new Card();
            Card target = createCreature("Bear", 2, 2, CardColor.GREEN);

            assertThat(evaluator.matchesCardPredicate(target,
                    new CardSharesCardTypeWithImprintedCardPredicate(), source.getId(), gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("CardSharesCardTypeWithImprintedCardPredicate compares types after a cost is paid")
        void cardTypeSharingPredicateUsesImprintedCardWhenPresent() {
            Card source = new Card();
            Card imprinted = createCreature("Elf", 1, 1, CardColor.GREEN);
            Card creature = createCreature("Bear", 2, 2, CardColor.GREEN);
            Card instant = new Card();
            instant.setType(CardType.INSTANT);
            gd.imprintedCards.put(source.getId(), imprinted);

            var predicate = new CardSharesCardTypeWithImprintedCardPredicate();
            assertThat(evaluator.matchesCardPredicate(creature, predicate, source.getId(), gd, player1Id)).isTrue();
            assertThat(evaluator.matchesCardPredicate(instant, predicate, source.getId(), gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("CardPowerToughnessTotalAtMostPredicate matches cards at or below the total")
        void cardPowerToughnessTotalAtMostPredicateMatches() {
            Card eligible = createCreature("Grizzly Bears", 2, 3, CardColor.GREEN);
            Card tooLarge = createCreature("Air Elemental", 4, 4, CardColor.BLUE);
            Card withoutPowerToughness = createLand("Forest");

            CardPowerToughnessTotalAtMostPredicate predicate = new CardPowerToughnessTotalAtMostPredicate(5);

            assertThat(evaluator.matchesCardPredicate(eligible, predicate, null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(tooLarge, predicate, null)).isFalse();
            assertThat(evaluator.matchesCardPredicate(withoutPowerToughness, predicate, null)).isFalse();
        }

        @Test
        @DisplayName("CardToughnessAtLeastPredicate matches cards at or above the toughness")
        void cardToughnessAtLeastPredicateMatches() {
            Card eligible = createCreature("Wall of Frost", 0, 7, CardColor.BLUE);
            Card tooSmall = createCreature("Grizzly Bears", 2, 2, CardColor.GREEN);
            Card withoutToughness = createLand("Forest");

            CardToughnessAtLeastPredicate predicate = new CardToughnessAtLeastPredicate(6);

            assertThat(evaluator.matchesCardPredicate(eligible, predicate, null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(tooSmall, predicate, null)).isFalse();
            assertThat(evaluator.matchesCardPredicate(withoutToughness, predicate, null)).isFalse();
        }

        @Test
        @DisplayName("CardSubtypePredicate matches subtype")
        void cardSubtypePredicateMatches() {
            Card card = createCreature("Elf", 1, 1, CardColor.GREEN);
            card.setSubtypes(List.of(CardSubtype.ELF));

            assertThat(evaluator.matchesCardPredicate(card, new CardSubtypePredicate(CardSubtype.ELF), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(card, new CardSubtypePredicate(CardSubtype.GOBLIN), null)).isFalse();
        }

        @Test
        @DisplayName("CardHasSourceChosenSubtypePredicate uses the source permanent's choice")
        void cardHasSourceChosenSubtypePredicateMatchesSourceChoice() {
            Card sourceCard = createArtifact("Belbe's Portal");
            Permanent source = addPermanent(player1Id, sourceCard);
            source.setChosenSubtype(CardSubtype.BEAR);

            Card bear = createCreatureWithSubtypes("Bear", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));
            Card elf = createCreatureWithSubtypes("Elf", 1, 1, CardColor.GREEN, List.of(CardSubtype.ELF));
            Card changeling = createChangelingCreature("Changeling");
            Card nonCreatureElf = createArtifact("Kindred Spell");
            nonCreatureElf.setSubtypes(List.of(CardSubtype.BEAR));
            CardHasSourceChosenSubtypePredicate predicate = new CardHasSourceChosenSubtypePredicate();

            assertThat(evaluator.matchesCardPredicate(bear, predicate, sourceCard.getId(), gd, player1Id)).isTrue();
            assertThat(evaluator.matchesCardPredicate(elf, predicate, sourceCard.getId(), gd, player1Id)).isFalse();
            assertThat(evaluator.matchesCardPredicate(changeling, predicate, sourceCard.getId(), gd, player1Id)).isTrue();
            assertThat(evaluator.matchesCardPredicate(nonCreatureElf, new CardHasSourceChosenSubtypePredicate(false),
                    sourceCard.getId(), gd, player1Id)).isTrue();
            assertThat(evaluator.matchesCardPredicate(nonCreatureElf, predicate, sourceCard.getId(), gd, player1Id))
                    .isFalse();
        }

        @Test
        @DisplayName("CardHasSourceChosenCardTypePredicate uses the source permanent's choice")
        void cardHasSourceChosenCardTypePredicateMatchesSourceChoice() {
            Card sourceCard = createArtifact("Arachne");
            Permanent source = addPermanent(player1Id, sourceCard);
            source.setChosenCardType(CardType.INSTANT);

            Card instant = new Card();
            instant.setType(CardType.INSTANT);
            Card sorcery = new Card();
            sorcery.setType(CardType.SORCERY);
            CardHasSourceChosenCardTypePredicate predicate = new CardHasSourceChosenCardTypePredicate();

            assertThat(evaluator.matchesCardPredicate(instant, predicate, sourceCard.getId(), gd, player1Id))
                    .isTrue();
            assertThat(evaluator.matchesCardPredicate(sorcery, predicate, sourceCard.getId(), gd, player1Id))
                    .isFalse();
        }

        @Test
        @DisplayName("CardHasSourceChosenColorPredicate matches every color of a multicolored card")
        void cardHasSourceChosenColorPredicateMatchesSourceChoice() {
            Card sourceCard = createArtifact("Jeweled Torque");
            Permanent source = addPermanent(player1Id, sourceCard);
            source.setChosenColor(CardColor.GREEN);

            Card green = createCreature("Green Creature", 2, 2, CardColor.GREEN);
            Card multicolored = createCreature("Green White Creature", 2, 2, CardColor.GREEN);
            multicolored.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
            Card red = createCreature("Red Creature", 2, 2, CardColor.RED);
            CardHasSourceChosenColorPredicate predicate = new CardHasSourceChosenColorPredicate();

            assertThat(evaluator.matchesCardPredicate(green, predicate, sourceCard.getId(), gd, player1Id)).isTrue();
            assertThat(evaluator.matchesCardPredicate(multicolored, predicate, sourceCard.getId(), gd, player1Id))
                    .isTrue();
            assertThat(evaluator.matchesCardPredicate(red, predicate, sourceCard.getId(), gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("CardNameInControllerGraveyardPredicate matches a name in the perspective player's graveyard")
        void cardNameInControllerGraveyardPredicateMatches() {
            Card matchingCard = createCreature("Shock", 0, 0, CardColor.RED);
            gd.playerGraveyards.get(player1Id).add(matchingCard);

            CardNameInControllerGraveyardPredicate predicate = new CardNameInControllerGraveyardPredicate();

            assertThat(evaluator.matchesCardPredicate(createCreature("Shock", 0, 0, CardColor.RED),
                    predicate, null, gd, player1Id)).isTrue();
            assertThat(evaluator.matchesCardPredicate(createCreature("Lightning Bolt", 0, 0, CardColor.RED),
                    predicate, null, gd, player1Id)).isFalse();
            assertThat(evaluator.matchesCardPredicate(createCreature("Shock", 0, 0, CardColor.RED),
                    predicate, null, gd, player2Id)).isFalse();
        }

        @Test
        @DisplayName("CardKeywordPredicate matches keyword")
        void cardKeywordPredicateMatches() {
            Card card = createMirranCrusader();

            assertThat(evaluator.matchesCardPredicate(card, new CardKeywordPredicate(Keyword.DOUBLE_STRIKE), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(card, new CardKeywordPredicate(Keyword.FLYING), null)).isFalse();
        }

        @Test
        @DisplayName("CardIsSelfPredicate matches source card")
        void cardIsSelfPredicateMatches() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(card, new CardIsSelfPredicate(), card.getId())).isTrue();
            assertThat(evaluator.matchesCardPredicate(card, new CardIsSelfPredicate(), UUID.randomUUID())).isFalse();
            assertThat(evaluator.matchesCardPredicate(card, new CardIsSelfPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("CardColorPredicate matches card color")
        void cardColorPredicateMatches() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(card, new CardColorPredicate(CardColor.GREEN), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(card, new CardColorPredicate(CardColor.RED), null)).isFalse();
        }

        @Test
        @DisplayName("CardColorPredicate matches every colour of a multicoloured card")
        void cardColorPredicateMatchesMulticolour() {
            Card card = createCreatureWithSubtypes("Golgari Spy", 2, 2, CardColor.BLACK, List.of());
            card.setColors(List.of(CardColor.BLACK, CardColor.GREEN));

            // A Black-Green card is both black and green (order-independent), not red.
            assertThat(evaluator.matchesCardPredicate(card, new CardColorPredicate(CardColor.BLACK), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(card, new CardColorPredicate(CardColor.GREEN), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(card, new CardColorPredicate(CardColor.RED), null)).isFalse();
        }

        @Test
        @DisplayName("CardIsMulticoloredPredicate matches only two-or-more-colour cards")
        void cardIsMulticoloredPredicateMatches() {
            Card multicolored = createCreatureWithSubtypes("Golgari Spy", 2, 2, CardColor.BLACK, List.of());
            multicolored.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
            Card monocolored = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));
            Card colorless = createCreatureWithSubtypes("Ornithopter", 0, 2, CardColor.GREEN, List.of());
            colorless.setColors(List.of());

            assertThat(evaluator.matchesCardPredicate(multicolored, new CardIsMulticoloredPredicate(), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(monocolored, new CardIsMulticoloredPredicate(), null)).isFalse();
            assertThat(evaluator.matchesCardPredicate(colorless, new CardIsMulticoloredPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("CardHasExactlyTwoColorsPredicate excludes cards with fewer or more colors")
        void cardHasExactlyTwoColorsPredicateMatchesOnlyExactlyTwoColors() {
            Card exactlyTwo = createCreatureWithSubtypes("Gold Creature", 2, 2, CardColor.BLACK, List.of());
            exactlyTwo.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
            Card threeColors = createCreatureWithSubtypes("Bant Creature", 2, 2, CardColor.WHITE, List.of());
            threeColors.setColors(List.of(CardColor.WHITE, CardColor.BLUE, CardColor.GREEN));
            Card oneColor = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of());

            assertThat(evaluator.matchesCardPredicate(exactlyTwo,
                    new CardHasExactlyTwoColorsPredicate(), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(threeColors,
                    new CardHasExactlyTwoColorsPredicate(), null)).isFalse();
            assertThat(evaluator.matchesCardPredicate(oneColor,
                    new CardHasExactlyTwoColorsPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("CardIsDoubleFacedPredicate excludes cards with modeled non-DFC faces")
        void cardIsDoubleFacedPredicateMatchesPhysicalDoubleFacedCards() {
            Card transforming = createCreature("Transforming Creature", 2, 2, CardColor.GREEN);
            transforming.setBackFaceCard(new Card());
            transforming.setKeywords(EnumSet.of(Keyword.TRANSFORM));

            Card battle = createBattle("Battle");
            battle.setBackFaceCard(new Card());

            Card split = new Card();
            split.setBackFaceCard(new Card());
            split.setKeywords(EnumSet.of(Keyword.AFTERMATH));

            Card meld = createCreature("Meld Card", 2, 2, CardColor.WHITE);
            meld.setBackFaceCard(new Card());
            meld.setKeywords(EnumSet.of(Keyword.MELD));

            assertThat(evaluator.matchesCardPredicate(transforming,
                    new CardIsDoubleFacedPredicate(), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(battle,
                    new CardIsDoubleFacedPredicate(), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(split,
                    new CardIsDoubleFacedPredicate(), null)).isFalse();
            assertThat(evaluator.matchesCardPredicate(meld,
                    new CardIsDoubleFacedPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("CardIsAuraPredicate matches aura cards")
        void cardIsAuraPredicateMatches() {
            Card aura = createAura("Heart of Light", new PreventAllDamageToAndByEnchantedCreatureEffect());
            Card nonAura = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(aura, new CardIsAuraPredicate(), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(nonAura, new CardIsAuraPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("CardAllOf(LAND, BASIC) matches only basic lands")
        void cardAllOfBasicLandMatches() {
            Card basicLand = createLand("Forest");
            basicLand.setSupertypes(Set.of(CardSupertype.BASIC));
            Card nonBasicLand = createLand("Wasteland");
            Card nonLand = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            CardAllOfPredicate basicLandFilter = new CardAllOfPredicate(List.of(
                    new CardSupertypePredicate(CardSupertype.BASIC),
                    new CardTypePredicate(CardType.LAND)));

            assertThat(evaluator.matchesCardPredicate(basicLand, basicLandFilter, null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(nonBasicLand, basicLandFilter, null)).isFalse();
            assertThat(evaluator.matchesCardPredicate(nonLand, basicLandFilter, null)).isFalse();
        }

        @Test
        @DisplayName("CardNotPredicate negates")
        void cardNotPredicateNegates() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(card, new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), null)).isFalse();
            assertThat(evaluator.matchesCardPredicate(card, new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT)), null)).isTrue();
        }

        @Test
        @DisplayName("CardAllOfPredicate requires all sub-predicates")
        void cardAllOfPredicateRequiresAll() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(card, new CardAllOfPredicate(List.of(
                    new CardTypePredicate(CardType.CREATURE),
                    new CardColorPredicate(CardColor.GREEN)
            )), null)).isTrue();

            assertThat(evaluator.matchesCardPredicate(card, new CardAllOfPredicate(List.of(
                    new CardTypePredicate(CardType.CREATURE),
                    new CardColorPredicate(CardColor.RED)
            )), null)).isFalse();
        }

        @Test
        @DisplayName("CardAnyOfPredicate requires any sub-predicate")
        void cardAnyOfPredicateRequiresAny() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(card, new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.ARTIFACT),
                    new CardColorPredicate(CardColor.GREEN)
            )), null)).isTrue();

            assertThat(evaluator.matchesCardPredicate(card, new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.ARTIFACT),
                    new CardColorPredicate(CardColor.RED)
            )), null)).isFalse();
        }
    }

    // ===== matchesPermanentPredicate =====

    @Nested
    @DisplayName("matchesPermanentPredicate")
    class MatchesPermanentPredicate {

        @Test
        @DisplayName("PermanentIsCreaturePredicate matches creature")
        void creaturePredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsCreaturePredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsCreaturePredicate rejects non-creature")
        void creaturePredicateRejectsNonCreature() {
            Permanent perm = addPermanent(player1Id, createLand("Forest"));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsCreaturePredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsSuspectedPredicate matches only suspected permanents")
        void suspectedPredicateMatchesOnlySuspectedPermanents() {
            Permanent suspected = addPermanent(player1Id,
                    createCreatureWithSubtypes("Skeleton", 2, 1, CardColor.BLACK,
                            List.of(CardSubtype.SKELETON)));
            Permanent unsuspected = addPermanent(player1Id,
                    createCreatureWithSubtypes("Skeleton", 2, 1, CardColor.BLACK,
                            List.of(CardSubtype.SKELETON)));
            suspected.setSuspected(true);

            assertThat(evaluator.matchesPermanentPredicate(gd, suspected,
                    new PermanentIsSuspectedPredicate())).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, unsuspected,
                    new PermanentIsSuspectedPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentAttachedToCreaturePredicate matches an attached permanent")
        void attachedToCreaturePredicateMatchesAttachedPermanent() {
            Permanent creature = addPermanent(player1Id,
                    createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Permanent attachment = addPermanent(player1Id, createArtifact("Dagger of the Worthy"));
            attachment.setAttachedTo(creature.getId());

            assertThat(evaluator.matchesPermanentPredicate(gd, attachment,
                    new PermanentAttachedToCreaturePredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentAttachedToCreaturePredicate rejects unattached permanents and non-creature hosts")
        void attachedToCreaturePredicateRejectsInvalidAttachments() {
            Permanent land = addPermanent(player1Id, createLand("Forest"));
            Permanent unattached = addPermanent(player1Id, createArtifact("Dagger of the Worthy"));
            Permanent attachedToLand = addPermanent(player1Id, createArtifact("Mox Emerald"));
            attachedToLand.setAttachedTo(land.getId());

            assertThat(evaluator.matchesPermanentPredicate(gd, unattached,
                    new PermanentAttachedToCreaturePredicate())).isFalse();
            assertThat(evaluator.matchesPermanentPredicate(gd, attachedToLand,
                    new PermanentAttachedToCreaturePredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsLandPredicate matches land")
        void landPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createLand("Forest"));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsLandPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsLandPredicate rejects non-land")
        void landPredicateRejectsNonLand() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsLandPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentControllerPoisonCountersAtLeastPredicate checks the target's current controller")
        void controllerPoisonCountersPredicateMatchesCurrentController() {
            Permanent target = addPermanent(player2Id, createCreature("Target", 2, 2, CardColor.GREEN));
            gd.playerPoisonCounters.put(player2Id, 3);

            PermanentControllerPoisonCountersAtLeastPredicate predicate =
                    new PermanentControllerPoisonCountersAtLeastPredicate(3);

            assertThat(evaluator.matchesPermanentPredicate(gd, target, predicate)).isTrue();

            gd.playerPoisonCounters.put(player2Id, 2);
            assertThat(evaluator.matchesPermanentPredicate(gd, target, predicate)).isFalse();
        }

        @Test
        @DisplayName("PermanentManaValueAtMostControlledCountPredicate counts matching permanents on the source controller's battlefield")
        void manaValueAtMostControlledCountMatches() {
            Card plainsOne = createLand("Plains");
            plainsOne.setSubtypes(List.of(CardSubtype.PLAINS));
            addPermanent(player1Id, plainsOne);
            Card plainsTwo = createLand("Snow-Covered Plains");
            plainsTwo.setSubtypes(List.of(CardSubtype.PLAINS));
            addPermanent(player1Id, plainsTwo);

            Card eligibleCard = createCreature("Grizzly Bears", 2, 2, CardColor.GREEN);
            eligibleCard.setManaCost("{2}");
            Permanent eligible = addPermanent(player2Id, eligibleCard);
            Card ineligibleCard = createCreature("Hill Giant", 3, 3, CardColor.RED);
            ineligibleCard.setManaCost("{3}");
            Permanent ineligible = addPermanent(player2Id, ineligibleCard);

            PermanentManaValueAtMostControlledCountPredicate predicate =
                    new PermanentManaValueAtMostControlledCountPredicate(
                            new PermanentHasSubtypePredicate(CardSubtype.PLAINS));
            FilterContext context = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(eligible, predicate, context)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(ineligible, predicate, context)).isFalse();
        }

        @Test
        @DisplayName("PermanentManaValueAtMostSourceControllerHandSizePredicate uses the source controller's hand")
        void manaValueAtMostSourceControllerHandSizeMatches() {
            gd.playerHands.get(player1Id).add(createArtifact("Hand Card One"));
            gd.playerHands.get(player1Id).add(createArtifact("Hand Card Two"));

            Card eligibleCard = createArtifact("Eligible Artifact");
            eligibleCard.setManaCost("{2}");
            Permanent eligible = addPermanent(player2Id, eligibleCard);
            Card ineligibleCard = createArtifact("Ineligible Artifact");
            ineligibleCard.setManaCost("{3}");
            Permanent ineligible = addPermanent(player2Id, ineligibleCard);

            PermanentManaValueAtMostSourceControllerHandSizePredicate predicate =
                    new PermanentManaValueAtMostSourceControllerHandSizePredicate();
            FilterContext context = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(eligible, predicate, context)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(ineligible, predicate, context)).isFalse();
        }

        @Test
        @DisplayName("PermanentManaValueAtMostControllerGraveyardCountPredicate uses the target's controller's graveyard")
        void manaValueAtMostControllerGraveyardCountMatches() {
            gd.playerGraveyards.get(player2Id).add(createArtifact("Graveyard Card One"));
            gd.playerGraveyards.get(player2Id).add(createArtifact("Graveyard Card Two"));

            Card eligibleCard = createArtifact("Eligible Artifact");
            eligibleCard.setManaCost("{2}");
            Permanent eligible = addPermanent(player2Id, eligibleCard);
            Card ineligibleCard = createArtifact("Ineligible Artifact");
            ineligibleCard.setManaCost("{3}");
            Permanent ineligible = addPermanent(player2Id, ineligibleCard);

            PermanentManaValueAtMostControllerGraveyardCountPredicate predicate =
                    new PermanentManaValueAtMostControllerGraveyardCountPredicate();
            FilterContext context = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(eligible, predicate, context)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(ineligible, predicate, context)).isFalse();
        }

        @Test
        @DisplayName("Lowest mana value nonland predicate ignores lands and allows ties")
        void lowestManaValueNonlandPredicateIgnoresLandsAndAllowsTies() {
            Card lowCard = createArtifact("Low Artifact");
            lowCard.setManaCost("{0}");
            Permanent low = addPermanent(player1Id, lowCard);
            Card tiedCard = createArtifact("Tied Artifact");
            tiedCard.setManaCost("{0}");
            Permanent tied = addPermanent(player2Id, tiedCard);
            Card highCard = createArtifact("High Artifact");
            highCard.setManaCost("{3}");
            Permanent high = addPermanent(player2Id, highCard);
            Permanent land = addPermanent(player1Id, createLand("Forest"));

            PermanentHasLowestManaValueAmongAllNonlandPermanentsPredicate predicate =
                    new PermanentHasLowestManaValueAmongAllNonlandPermanentsPredicate();
            assertThat(evaluator.matchesPermanentPredicate(gd, low, predicate)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, tied, predicate)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, high, predicate)).isFalse();
            assertThat(evaluator.matchesPermanentPredicate(gd, land, predicate)).isFalse();
        }

        @Test
        @DisplayName("PermanentHasNonManaActivatedAbilityPredicate matches a land with a non-mana ability")
        void nonManaActivatedAbilityPredicateMatches() {
            Card utilityLand = createLand("Utility Land");
            utilityLand.addActivatedAbility(new ActivatedAbility(false, null,
                    List.of(new DrawCardEffect()), "Draw a card."));
            Permanent perm = addPermanent(player1Id, utilityLand);

            assertThat(evaluator.matchesPermanentPredicate(
                    gd, perm, new PermanentHasNonManaActivatedAbilityPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentHasNonManaActivatedAbilityPredicate rejects a land with only mana abilities")
        void nonManaActivatedAbilityPredicateRejectsManaOnlyLand() {
            Card manaLand = createLand("Mana Land");
            manaLand.addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
            Permanent perm = addPermanent(player1Id, manaLand);

            assertThat(evaluator.matchesPermanentPredicate(
                    gd, perm, new PermanentHasNonManaActivatedAbilityPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentHasTapActivatedAbilityPredicate matches only abilities with a tap cost")
        void tapActivatedAbilityPredicateMatchesOnlyTapAbilities() {
            Card tapCard = createCreature("Tap Creature", 1, 1, CardColor.GREEN);
            tapCard.addActivatedAbility(new ActivatedAbility(true, null,
                    List.of(new DrawCardEffect()), "{T}: Draw a card."));
            Permanent tapPermanent = addPermanent(player1Id, tapCard);

            Card noTapCard = createCreature("No Tap Creature", 1, 1, CardColor.GREEN);
            noTapCard.addActivatedAbility(new ActivatedAbility(false, "{1}",
                    List.of(new DrawCardEffect()), "{1}: Draw a card."));
            Permanent noTapPermanent = addPermanent(player1Id, noTapCard);

            PermanentHasTapActivatedAbilityPredicate predicate = new PermanentHasTapActivatedAbilityPredicate();
            assertThat(evaluator.matchesPermanentPredicate(gd, tapPermanent, predicate)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, noTapPermanent, predicate)).isFalse();
        }

        @Test
        @DisplayName("Level-up ability predicate matches level-up abilities only")
        void levelUpAbilityPredicateMatchesLevelUpAbilitiesOnly() {
            Card levelUpCreature = createCreature("Leveler", 1, 1, CardColor.BLUE);
            levelUpCreature.addActivatedAbility(new ActivatedAbility(false, "{2}",
                    List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                    "Level up {2} ({2}: Put a level counter on this.)"));
            Permanent levelUpPermanent = addPermanent(player1Id, levelUpCreature);

            Card ordinaryCreature = createCreature("Ordinary Creature", 1, 1, CardColor.BLUE);
            ordinaryCreature.addActivatedAbility(new ActivatedAbility(false, "{1}",
                    List.of(new DrawCardEffect()), "Draw a card."));
            Permanent ordinaryPermanent = addPermanent(player1Id, ordinaryCreature);

            assertThat(evaluator.matchesPermanentPredicate(gd, levelUpPermanent,
                    PermanentHasNonManaActivatedAbilityPredicate.levelUp())).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, ordinaryPermanent,
                    PermanentHasNonManaActivatedAbilityPredicate.levelUp())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsArtifactPredicate matches artifact")
        void artifactPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createArtifact("Angel's Feather"));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsArtifactPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsArtifactPredicate rejects non-artifact")
        void artifactPredicateRejectsNonArtifact() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsArtifactPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsMonocoloredPredicate matches a single-colored permanent")
        void monocoloredPredicateMatchesMonocolored() {
            Permanent perm = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsMonocoloredPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsMonocoloredPredicate rejects a colorless permanent")
        void monocoloredPredicateRejectsColorless() {
            Permanent perm = addPermanent(player1Id, createCreature("Ornithopter", 0, 2, null));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsMonocoloredPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsMonocoloredPredicate rejects a multicolored permanent")
        void monocoloredPredicateRejectsMulticolored() {
            Card gold = createCreature("Gold Hybrid", 2, 2, CardColor.BLACK);
            gold.setColors(List.of(CardColor.BLACK, CardColor.RED));
            Permanent perm = addPermanent(player1Id, gold);

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsMonocoloredPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsColorlessPredicate matches a colorless permanent")
        void colorlessPredicateMatchesColorless() {
            Permanent perm = addPermanent(player1Id, createCreature("Ornithopter", 0, 2, null));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsColorlessPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsColorlessPredicate rejects a colored permanent")
        void colorlessPredicateRejectsColored() {
            Permanent perm = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsColorlessPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsMulticoloredPredicate matches a two-or-more-colored permanent")
        void multicoloredPredicateMatchesMulticolored() {
            Card gold = createCreature("Gold Hybrid", 2, 2, CardColor.BLACK);
            gold.setColors(List.of(CardColor.BLACK, CardColor.RED));
            Permanent perm = addPermanent(player1Id, gold);

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsMulticoloredPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsMulticoloredPredicate rejects a monocolored permanent")
        void multicoloredPredicateRejectsMonocolored() {
            Permanent perm = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsMulticoloredPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsMulticoloredPredicate rejects a colorless permanent")
        void multicoloredPredicateRejectsColorless() {
            Permanent perm = addPermanent(player1Id, createCreature("Ornithopter", 0, 2, null));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsMulticoloredPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentHasExactlyTwoColorsPredicate excludes a three-colored permanent")
        void permanentHasExactlyTwoColorsPredicateMatchesOnlyExactlyTwoColors() {
            Card exactlyTwo = createCreature("Gold Creature", 2, 2, CardColor.BLACK);
            exactlyTwo.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
            Card threeColors = createCreature("Bant Creature", 2, 2, CardColor.WHITE);
            threeColors.setColors(List.of(CardColor.WHITE, CardColor.BLUE, CardColor.GREEN));

            assertThat(evaluator.matchesPermanentPredicate(gd, addPermanent(player1Id, exactlyTwo),
                    new PermanentHasExactlyTwoColorsPredicate())).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, addPermanent(player1Id, threeColors),
                    new PermanentHasExactlyTwoColorsPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsEnchantmentPredicate matches enchantment")
        void enchantmentPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createEnchantmentWithStaticEffect("Furnace of Rath", new DoubleDamageEffect()));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsEnchantmentPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsEnchantmentPredicate rejects non-enchantment")
        void enchantmentPredicateRejectsNonEnchantment() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsEnchantmentPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsTappedPredicate matches tapped permanent")
        void tappedPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.tap();

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsTappedPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsTappedPredicate rejects untapped permanent")
        void tappedPredicateRejectsUntapped() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsTappedPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentActivatedThisTurnPredicate matches an activated permanent")
        void activatedThisTurnPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreature("Activated Permanent", 2, 2, CardColor.GREEN));
            gd.activatedAbilityUsesThisTurn.put(perm.getId(), Map.of(0, 1));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentActivatedThisTurnPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentActivatedThisTurnPredicate rejects a permanent without an activation")
        void activatedThisTurnPredicateRejectsUnactivated() {
            Permanent perm = addPermanent(player1Id, createCreature("Unactivated Permanent", 2, 2, CardColor.GREEN));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentActivatedThisTurnPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsTokenPredicate matches token")
        void tokenPredicateMatches() {
            Card tokenCard = createCreature("Soldier Token", 1, 1, CardColor.WHITE);
            tokenCard.setToken(true);
            Permanent perm = addPermanent(player1Id, tokenCard);

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsTokenPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsTokenPredicate rejects non-token")
        void tokenPredicateRejectsNonToken() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsTokenPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsTransformedPredicate matches a transformed permanent")
        void transformedPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreature("Transformed Creature", 2, 2, CardColor.GREEN));
            perm.setTransformed(true);

            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentIsTransformedPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsTransformedPredicate rejects an untransformed permanent")
        void transformedPredicateRejectsUntransformed() {
            Permanent perm = addPermanent(player1Id, createCreature("Front Face", 2, 2, CardColor.GREEN));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentIsTransformedPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsAttackingPredicate matches attacking creature")
        void attackingPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setAttacking(true);

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsAttackingPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsAttackingPredicate rejects non-attacking")
        void attackingPredicateRejectsNonAttacking() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsAttackingPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsUnblockedAttackingPredicate matches after blockers are declared")
        void unblockedAttackingMatchesAfterBlockers() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setAttacking(true);
            gd.currentStep = TurnStep.DECLARE_BLOCKERS;

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsUnblockedAttackingPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsUnblockedAttackingPredicate rejects before blockers are declared")
        void unblockedAttackingRejectsBeforeBlockers() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setAttacking(true);
            gd.currentStep = TurnStep.DECLARE_ATTACKERS;

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsUnblockedAttackingPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsBlockingPredicate matches blocking creature")
        void blockingPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setBlocking(true);

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsBlockingPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsBlockingPredicate rejects non-blocking")
        void blockingPredicateRejectsNonBlocking() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsBlockingPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate matches subtype")
        void subtypePredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.BEAR))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate rejects non-matching subtype")
        void subtypePredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.ELF))).isFalse();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate matches changeling for creature subtypes")
        void subtypePredicateMatchesChangeling() {
            Permanent perm = addPermanent(player1Id, createChangelingCreature("Changeling Wayfinder"));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.ELF))).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate changeling does not match non-creature subtypes")
        void changelingDoesNotMatchNonCreatureSubtypes() {
            Permanent perm = addPermanent(player1Id, createChangelingCreature("Changeling Wayfinder"));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))).isFalse();
            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.AURA))).isFalse();
        }

        @Test
        @DisplayName("PermanentHasAnySubtypePredicate matches any matching subtype")
        void hasAnySubtypePredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentHasAnySubtypePredicate(EnumSet.of(CardSubtype.BEAR, CardSubtype.ELF)))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasAnySubtypePredicate rejects when no subtypes match")
        void hasAnySubtypePredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentHasAnySubtypePredicate(EnumSet.of(CardSubtype.ELF, CardSubtype.GOBLIN)))).isFalse();
        }

        @Test
        @DisplayName("PermanentHasKeywordPredicate matches keyword")
        void keywordPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createMirranCrusader());

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentHasKeywordPredicate(Keyword.DOUBLE_STRIKE))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasKeywordPredicate rejects non-matching keyword")
        void keywordPredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentHasKeywordPredicate(Keyword.FLYING))).isFalse();
        }

        @Test
        @DisplayName("PermanentPowerAtMostPredicate matches when power is at or below threshold")
        void powerAtMostPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR))); // power 2

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentPowerAtMostPredicate(2))).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentPowerAtMostPredicate(3))).isTrue();
        }

        @Test
        @DisplayName("PermanentPowerAtMostPredicate rejects when power exceeds threshold")
        void powerAtMostPredicateRejectsAboveThreshold() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR))); // power 2

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentPowerAtMostPredicate(1))).isFalse();
        }

        @Test
        @DisplayName("PermanentToughnessGreaterThanPowerPredicate matches only strict toughness superiority")
        void toughnessGreaterThanPowerPredicateMatchesStrictly() {
            Permanent greaterToughness = addPermanent(player1Id,
                    createCreature("Giant Spider", 2, 4, CardColor.GREEN));
            Permanent equalPowerToughness = addPermanent(player1Id,
                    createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            Permanent greaterPower = addPermanent(player1Id,
                    createCreature("Goblin Piker", 2, 1, CardColor.RED));

            PermanentToughnessGreaterThanPowerPredicate predicate =
                    new PermanentToughnessGreaterThanPowerPredicate();
            assertThat(evaluator.matchesPermanentPredicate(gd, greaterToughness, predicate)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, equalPowerToughness, predicate)).isFalse();
            assertThat(evaluator.matchesPermanentPredicate(gd, greaterPower, predicate)).isFalse();
        }

        @Test
        @DisplayName("greatest power among controller's creatures is evaluated per permanent controller")
        void greatestPowerAmongControllerCreatures() {
            Permanent p1Small = addPermanent(player1Id,
                    createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            Permanent p1Big = addPermanent(player1Id,
                    createCreature("Hill Giant", 3, 3, CardColor.RED));
            Permanent p2Creature = addPermanent(player2Id,
                    createCreature("Craw Wurm", 4, 4, CardColor.GREEN));
            PermanentHasGreatestPowerAmongControllerCreaturesPredicate predicate =
                    new PermanentHasGreatestPowerAmongControllerCreaturesPredicate();

            assertThat(evaluator.matchesPermanentPredicate(gd, p1Small, predicate)).isFalse();
            assertThat(evaluator.matchesPermanentPredicate(gd, p1Big, predicate)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, p2Creature, predicate)).isTrue();
        }

        @Test
        @DisplayName("greatest power predicate includes a creature that has left the battlefield")
        void greatestPowerIncludesRemovedCreature() {
            Permanent dyingCreature = new Permanent(createCreature("Hill Giant", 3, 3, CardColor.RED));
            addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            PermanentHasGreatestPowerAmongControllerCreaturesPredicate predicate =
                    new PermanentHasGreatestPowerAmongControllerCreaturesPredicate();
            FilterContext context = FilterContext.of(gd).withSourceControllerId(player2Id);

            assertThat(evaluator.matchesPermanentPredicate(dyingCreature, predicate, context)).isTrue();
        }

        @Test
        @DisplayName("PermanentPowerAtMostSourcePowerPredicate matches when target power is at or below source power")
        void powerAtMostSourcePowerMatches() {
            Card source = createCreatureWithSubtypes("Hill Giant", 3, 3, CardColor.RED, List.of(CardSubtype.GIANT)); // power 3
            addPermanent(player1Id, source);
            Permanent target = addPermanent(player2Id, createCreatureWithSubtypes("Hill Giant", 3, 3, CardColor.RED, List.of(CardSubtype.GIANT))); // power 3
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(source.getId());

            assertThat(evaluator.matchesPermanentPredicate(target, new PermanentPowerAtMostSourcePowerPredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentPowerAtMostSourcePowerPredicate rejects when target power exceeds source power")
        void powerAtMostSourcePowerRejectsAbove() {
            Card source = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)); // power 2
            addPermanent(player1Id, source);
            Permanent target = addPermanent(player2Id, createCreatureWithSubtypes("Hill Giant", 3, 3, CardColor.RED, List.of(CardSubtype.GIANT))); // power 3
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(source.getId());

            assertThat(evaluator.matchesPermanentPredicate(target, new PermanentPowerAtMostSourcePowerPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentPowerLessThanSourcePowerPredicate matches only strictly below source power")
        void powerLessThanSourcePower() {
            Card source = createCreatureWithSubtypes("Hill Giant", 3, 3, CardColor.RED, List.of(CardSubtype.GIANT)); // power 3
            addPermanent(player1Id, source);
            Permanent weaker = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR))); // power 2
            Permanent equal = addPermanent(player2Id, createCreatureWithSubtypes("Hill Giant", 3, 3, CardColor.RED, List.of(CardSubtype.GIANT))); // power 3
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(source.getId());

            assertThat(evaluator.matchesPermanentPredicate(weaker, new PermanentPowerLessThanSourcePowerPredicate(), ctx)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(equal, new PermanentPowerLessThanSourcePowerPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentPowerLessThanControllerGraveyardCountPredicate uses the source controller's graveyard and is strict")
        void powerLessThanControllerGraveyardCount() {
            Permanent target = addPermanent(player2Id,
                    createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            gd.playerGraveyards.get(player1Id).addAll(List.of(new Card(), new Card(), new Card()));
            gd.playerGraveyards.get(player2Id).addAll(List.of(new Card(), new Card(), new Card(), new Card()));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);
            PermanentPowerLessThanControllerGraveyardCountPredicate predicate =
                    new PermanentPowerLessThanControllerGraveyardCountPredicate();

            assertThat(evaluator.matchesPermanentPredicate(target, predicate, ctx)).isTrue();

            gd.playerGraveyards.get(player1Id).remove(2);
            assertThat(evaluator.matchesPermanentPredicate(target, predicate, ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentColorInPredicate matches color")
        void colorInPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.GREEN)))).isTrue();
        }

        @Test
        @DisplayName("PermanentColorInPredicate rejects non-matching color")
        void colorInPredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.RED)))).isFalse();
        }

        @Test
        @DisplayName("PermanentColorInPredicate matches overridden color")
        void colorInPredicateMatchesOverriddenColor() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setColorOverridden(true);
            perm.getTransientColors().add(CardColor.BLUE);

            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.BLUE)))).isTrue();
            // Original color should not match when overridden
            assertThat(evaluator.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.GREEN)))).isFalse();
        }

        @Test
        @DisplayName("PermanentSharesMostCommonColorPredicate matches a tied color")
        void sharesMostCommonColorMatchesTiedColor() {
            Permanent target = addPermanent(player1Id,
                    createCreature("White Creature", 1, 1, CardColor.WHITE));
            addPermanent(player2Id, createCreature("Blue Creature", 1, 1, CardColor.BLUE));

            assertThat(evaluator.matchesPermanentPredicate(gd, target,
                    new PermanentSharesMostCommonColorPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentSharesMostCommonColorPredicate counts every color of a multicolored permanent")
        void sharesMostCommonColorCountsMulticoloredPermanents() {
            Permanent target = addPermanent(player1Id,
                    createCreature("White Creature", 1, 1, CardColor.WHITE));
            Card blueRed = createCreature("Blue Red Creature", 1, 1, CardColor.BLUE);
            blueRed.setColors(List.of(CardColor.BLUE, CardColor.RED));
            addPermanent(player2Id, blueRed);
            Card secondBlueRed = createCreature("Second Blue Red Creature", 1, 1, CardColor.BLUE);
            secondBlueRed.setColors(List.of(CardColor.BLUE, CardColor.RED));
            addPermanent(player2Id, secondBlueRed);

            assertThat(evaluator.matchesPermanentPredicate(gd, target,
                    new PermanentSharesMostCommonColorPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentSharesMostCommonColorPredicate rejects a less common color")
        void sharesMostCommonColorRejectsLessCommonColor() {
            Permanent target = addPermanent(player1Id,
                    createCreature("White Creature", 1, 1, CardColor.WHITE));
            addPermanent(player2Id, createCreature("Blue Creature 1", 1, 1, CardColor.BLUE));
            addPermanent(player2Id, createCreature("Blue Creature 2", 1, 1, CardColor.BLUE));

            assertThat(evaluator.matchesPermanentPredicate(gd, target,
                    new PermanentSharesMostCommonColorPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentDealtDamageThisTurnPredicate matches permanent dealt damage this turn")
        void dealtDamageThisTurnPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            gd.permanentsDealtDamageThisTurn.add(perm.getId());

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentDealtDamageThisTurnPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentDealtDamageThisTurnPredicate rejects permanent not dealt damage this turn")
        void dealtDamageThisTurnPredicateRejects() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentDealtDamageThisTurnPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentDealtDamageThisTurnPredicate returns false when gameData is null")
        void dealtDamageThisTurnPredicateReturnsFalseWithNullGameData() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            gd.permanentsDealtDamageThisTurn.add(perm.getId());

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentDealtDamageThisTurnPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("PermanentTruePredicate always returns true")
        void truePredicateAlwaysTrue() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentTruePredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentNotPredicate negates")
        void notPredicateNegates() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentNotPredicate(new PermanentIsCreaturePredicate()))).isFalse();
            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentNotPredicate(new PermanentIsArtifactPredicate()))).isTrue();
        }

        @Test
        @DisplayName("PermanentAllOfPredicate requires all")
        void allOfPredicateRequiresAll() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentHasSubtypePredicate(CardSubtype.BEAR)
            )))).isTrue();

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentIsArtifactPredicate()
            )))).isFalse();
        }

        @Test
        @DisplayName("PermanentAnyOfPredicate requires any")
        void anyOfPredicateRequiresAny() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentAnyOfPredicate(List.of(
                    new PermanentIsArtifactPredicate(),
                    new PermanentIsCreaturePredicate()
            )))).isTrue();

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentAnyOfPredicate(List.of(
                    new PermanentIsArtifactPredicate(),
                    new PermanentIsLandPredicate()
            )))).isFalse();
        }

        @Test
        @DisplayName("PermanentIsSourceCardPredicate matches source card")
        void sourceCardPredicateMatches() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));
            Permanent perm = addPermanent(player1Id, card);
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(card.getId());

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentIsSourceCardPredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentIsSourceCardPredicate rejects different card")
        void sourceCardPredicateRejectsDifferent() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(UUID.randomUUID());

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentIsSourceCardPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentHasSourceChosenSubtypePredicate matches a permanent with the source's chosen subtype")
        void sourceChosenSubtypeMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Card sourceCard = createCreature("Shimmer Source", 1, 1, CardColor.BLUE);
            Permanent source = addPermanent(player1Id, sourceCard);
            source.setChosenSubtype(CardSubtype.BEAR);
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(sourceCard.getId());

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentHasSourceChosenSubtypePredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentHasSourceChosenSubtypePredicate rejects a different subtype and an unchosen source")
        void sourceChosenSubtypeRejects() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Card sourceCard = createCreature("Shimmer Source", 1, 1, CardColor.BLUE);
            Permanent source = addPermanent(player1Id, sourceCard);
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(sourceCard.getId());

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentHasSourceChosenSubtypePredicate(), ctx)).isFalse();

            source.setChosenSubtype(CardSubtype.GOBLIN);
            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentHasSourceChosenSubtypePredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentHasSourceChosenColorPredicate matches a permanent with the source's chosen color")
        void sourceChosenColorMatches() {
            Permanent perm = addPermanent(player1Id, createCreature("Red Creature", 2, 2, CardColor.RED));
            Card sourceCard = createCreature("Teferi's Moat", 0, 0, CardColor.WHITE);
            Permanent source = addPermanent(player1Id, sourceCard);
            source.setChosenColor(CardColor.RED);
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(sourceCard.getId());

            assertThat(evaluator.matchesPermanentPredicate(perm,
                    new PermanentHasSourceChosenColorPredicate(), ctx)).isTrue();

            assertThat(evaluator.matchesPermanentPredicate(perm,
                    new PermanentHasSourceChosenColorPredicate(),
                    FilterContext.empty().withSourcePermanentSnapshot(source))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasSourceChosenColorPredicate rejects another color and an unchosen source")
        void sourceChosenColorRejects() {
            Permanent perm = addPermanent(player1Id, createCreature("Red Creature", 2, 2, CardColor.RED));
            Card sourceCard = createCreature("Teferi's Moat", 0, 0, CardColor.WHITE);
            Permanent source = addPermanent(player1Id, sourceCard);
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(sourceCard.getId());

            assertThat(evaluator.matchesPermanentPredicate(perm,
                    new PermanentHasSourceChosenColorPredicate(), ctx)).isFalse();

            source.setChosenColor(CardColor.BLUE);
            assertThat(evaluator.matchesPermanentPredicate(perm,
                    new PermanentHasSourceChosenColorPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentControlledBySourceControllerPredicate matches controlled permanent")
        void controlledBySourceControllerMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentControlledBySourceControllerPredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentControlledBySourceControllerPredicate rejects opponent's permanent")
        void controlledBySourceControllerRejectsOpponent() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentControlledBySourceControllerPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentOwnedBySourceControllerPredicate matches a permanent the source controller owns")
        void ownedBySourceControllerMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentOwnedBySourceControllerPredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentOwnedBySourceControllerPredicate rejects a permanent owned by another player")
        void ownedBySourceControllerRejectsOpponent() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentOwnedBySourceControllerPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentOwnedBySourceControllerPredicate matches a permanent the source controller owns but does not control")
        void ownedBySourceControllerMatchesStolenFromController() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            gd.stolenCreatures.put(perm.getId(), player1Id);
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(perm, new PermanentOwnedBySourceControllerPredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentControllerControlsPermanentPredicate matches when the target's controller controls a matching permanent")
        void controllerControlsPermanentMatches() {
            Permanent bear = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            addPermanent(player2Id, createCreatureWithSubtypes("Island", 0, 0, CardColor.BLUE, List.of(CardSubtype.ISLAND)));

            assertThat(evaluator.matchesPermanentPredicate(bear,
                    new PermanentControllerControlsPermanentPredicate(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                    FilterContext.of(gd))).isTrue();
        }

        @Test
        @DisplayName("PermanentControllerControlsPermanentPredicate rejects when only another player controls the matching permanent")
        void controllerControlsPermanentRejects() {
            Permanent bear = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            // The Island belongs to player1, but the target creature is controlled by player2.
            addPermanent(player1Id, createCreatureWithSubtypes("Island", 0, 0, CardColor.BLUE, List.of(CardSubtype.ISLAND)));

            assertThat(evaluator.matchesPermanentPredicate(bear,
                    new PermanentControllerControlsPermanentPredicate(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                    FilterContext.of(gd))).isFalse();
        }

        @Test
        @DisplayName("PermanentIsPlaneswalkerPredicate rejects non-planeswalker")
        void planeswalkerPredicateRejectsNonPlaneswalker() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsPlaneswalkerPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsBattlePredicate accepts a battle and rejects a creature")
        void battlePredicateMatchesOnlyBattles() {
            Permanent battle = addPermanent(player1Id, createBattle("Invasion of Somewhere"));
            Permanent bear = addPermanent(player1Id,
                    createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, battle, new PermanentIsBattlePredicate())).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, bear, new PermanentIsBattlePredicate())).isFalse();
        }

        /**
         * Without its own arm in the layered overload the predicate falls through to the default,
         * which re-asks the non-layered path and so ignores the layer-4 types computed so far.
         */
        @Test
        @DisplayName("PermanentIsBattlePredicate is answered from the layered state during the CR 613 pass")
        void battlePredicateReadsTheLayeredState() {
            Permanent bear = addPermanent(player1Id,
                    createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            CharacteristicState state = new CharacteristicState(bear.getCard(), bear);
            state.overrideCardTypes(List.of(CardType.BATTLE));

            assertThat(evaluator.matchesPermanentPredicate(
                    state, bear, new PermanentIsBattlePredicate(), FilterContext.of(gd))).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(
                    state, bear, new PermanentIsCreaturePredicate(), FilterContext.of(gd))).isFalse();
        }
    }

    // ===== matchesFilters =====

    @Nested
    @DisplayName("matchesFilters")
    class MatchesFilters {

        @Test
        @DisplayName("PermanentPredicateTargetFilter passes when predicate matches")
        void permanentFilterPasses() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesFilters(gd, perm, Set.of(
                    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be creature")
            ))).isTrue();
        }

        @Test
        @DisplayName("PermanentPredicateTargetFilter fails when predicate doesn't match")
        void permanentFilterFails() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesFilters(gd, perm, Set.of(
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Must be artifact")
            ))).isFalse();
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter passes for controlled permanent")
        void controlledFilterPasses() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesFilters(perm, Set.of(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature")
            ), ctx)).isTrue();
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter fails for opponent's permanent")
        void controlledFilterFailsForOpponent() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(evaluator.matchesFilters(perm, Set.of(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature")
            ), ctx)).isFalse();
        }

        @Test
        @DisplayName("multiple filters must all match")
        void multipleFiltersMustAllMatch() {
            Permanent perm = addPermanent(player1Id, createArtifactCreature("Myr Sire", 1, 1, List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR)));

            assertThat(evaluator.matchesFilters(gd, perm, Set.of(
                    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be creature"),
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Must be artifact")
            ))).isTrue();
        }
    }

    // ===== validateTargetFilter =====

    @Nested
    @DisplayName("validateTargetFilter")
    class ValidateTargetFilter {

        @Test
        @DisplayName("PermanentPredicateTargetFilter passes when matches")
        void permanentFilterPasses() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            evaluator.validateTargetFilter(gd,
                    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be creature"),
                    perm);
        }

        @Test
        @DisplayName("PermanentPredicateTargetFilter throws when doesn't match")
        void permanentFilterThrows() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThatThrownBy(() -> evaluator.validateTargetFilter(gd,
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Must be artifact"),
                    perm))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must be artifact");
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter passes for controlled permanent")
        void controlledFilterPasses() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            evaluator.validateTargetFilter(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature"),
                    perm, ctx);
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter throws when not controlled")
        void controlledFilterThrowsWhenNotControlled() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThatThrownBy(() -> evaluator.validateTargetFilter(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature"),
                    perm, ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must be controlled creature");
        }

        @Test
        @DisplayName("OwnedPermanentPredicateTargetFilter passes for owned permanent")
        void ownedFilterPasses() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            evaluator.validateTargetFilter(
                    new OwnedPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be owned creature"),
                    perm, ctx);
        }

        @Test
        @DisplayName("OwnedPermanentPredicateTargetFilter throws when not owned")
        void ownedFilterThrowsWhenNotOwned() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThatThrownBy(() -> evaluator.validateTargetFilter(
                    new OwnedPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be owned creature"),
                    perm, ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must be owned creature");
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter throws when gameData is null")
        void controlledFilterThrowsWithNullGameData() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThatThrownBy(() -> evaluator.validateTargetFilter(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Error"),
                    perm, FilterContext.empty()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("matchesStackEntryPredicate — enchanted-player filter")
    class MatchesStackEntryPredicateEnchantedPlayer {

        @Test
        @DisplayName("matches a stack entry with the requested supertype")
        void matchesSupertype() {
            Card legendaryInstant = new Card();
            legendaryInstant.setName("Legendary Instant");
            legendaryInstant.setType(CardType.INSTANT);
            legendaryInstant.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            StackEntry legendaryEntry = new StackEntry(
                    StackEntryType.INSTANT_SPELL, legendaryInstant, player1Id,
                    "Legendary Instant", new ArrayList<>());

            Card ordinaryInstant = new Card();
            ordinaryInstant.setName("Ordinary Instant");
            ordinaryInstant.setType(CardType.INSTANT);
            StackEntry ordinaryEntry = new StackEntry(
                    StackEntryType.INSTANT_SPELL, ordinaryInstant, player1Id,
                    "Ordinary Instant", new ArrayList<>());

            StackEntrySupertypeInPredicate predicate = new StackEntrySupertypeInPredicate(
                    Set.of(CardSupertype.LEGENDARY));
            assertThat(evaluator.matchesStackEntryPredicate(legendaryEntry, predicate, null)).isTrue();
            assertThat(evaluator.matchesStackEntryPredicate(ordinaryEntry, predicate, null)).isFalse();
        }

        @Test
        @DisplayName("matches a stack entry by maximum mana value")
        void matchesMaximumManaValue() {
            Card cheapInstant = new Card();
            cheapInstant.setName("Cheap Instant");
            cheapInstant.setType(CardType.INSTANT);
            cheapInstant.setManaCost("{1}{U}");
            StackEntry cheapEntry = new StackEntry(
                    StackEntryType.INSTANT_SPELL, cheapInstant, player1Id,
                    "Cheap Instant", new ArrayList<>());

            Card expensiveInstant = new Card();
            expensiveInstant.setName("Expensive Instant");
            expensiveInstant.setType(CardType.INSTANT);
            expensiveInstant.setManaCost("{4}{U}");
            StackEntry expensiveEntry = new StackEntry(
                    StackEntryType.INSTANT_SPELL, expensiveInstant, player1Id,
                    "Expensive Instant", new ArrayList<>());

            StackEntryMaxManaValuePredicate predicate = new StackEntryMaxManaValuePredicate(3);
            assertThat(evaluator.matchesStackEntryPredicate(cheapEntry, predicate, null)).isTrue();
            assertThat(evaluator.matchesStackEntryPredicate(expensiveEntry, predicate, null)).isFalse();
        }

        private StackEntry instantControlledBy(UUID controllerId) {
            Card bolt = new Card();
            bolt.setName("Lightning Bolt");
            bolt.setType(CardType.INSTANT);
            return new StackEntry(StackEntryType.INSTANT_SPELL, bolt, controllerId, "Lightning Bolt", new ArrayList<>());
        }

        @Test
        @DisplayName("matches when the entry is controlled by the enchanted player")
        void matchesWhenControlledByEnchantedPlayer() {
            StackEntry entry = instantControlledBy(player2Id);

            boolean result = evaluator.matchesStackEntryPredicate(
                    entry, new StackEntryControlledByEnchantedPlayerPredicate(), player2Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("does not match when the entry is controlled by another player")
        void doesNotMatchWhenControlledByAnotherPlayer() {
            StackEntry entry = instantControlledBy(player1Id);

            boolean result = evaluator.matchesStackEntryPredicate(
                    entry, new StackEntryControlledByEnchantedPlayerPredicate(), player2Id);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("does not match when there is no enchanted-player context")
        void doesNotMatchWithoutEnchantedPlayerContext() {
            StackEntry entry = instantControlledBy(player2Id);

            boolean result = evaluator.matchesStackEntryPredicate(
                    entry, new StackEntryControlledByEnchantedPlayerPredicate(), null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("AllOf combines instant/sorcery type with the enchanted-player filter")
        void allOfTypeAndEnchantedPlayer() {
            StackEntryAllOfPredicate filter = new StackEntryAllOfPredicate(List.of(
                    new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                    new StackEntryControlledByEnchantedPlayerPredicate()));

            // Instant cast by the enchanted player → matches.
            assertThat(evaluator.matchesStackEntryPredicate(instantControlledBy(player2Id), filter, player2Id)).isTrue();
            // Instant cast by someone else → fails the enchanted-player clause.
            assertThat(evaluator.matchesStackEntryPredicate(instantControlledBy(player1Id), filter, player2Id)).isFalse();

            // Creature cast by the enchanted player → fails the type clause.
            Card bears = new Card();
            bears.setName("Grizzly Bears");
            bears.setType(CardType.CREATURE);
            StackEntry creature = new StackEntry(
                    StackEntryType.CREATURE_SPELL, bears, player2Id, "Grizzly Bears", new ArrayList<>());
            assertThat(evaluator.matchesStackEntryPredicate(creature, filter, player2Id)).isFalse();
        }
    }

    /**
     * The recursion-safe funnel the static effect handlers reach through
     * {@code StaticEffectSupport.matchesStaticFilter}. No CR 613 pass is active here, so every
     * leaf takes its outside-a-pass branch — the state-answered branches are covered end to end
     * by {@code SevenLayerTest} and the card tests. The context carries a {@code GameData}, as it
     * does from every handler, which is also what proves the leaves ignore it.
     */
    @Nested
    @DisplayName("matchesStaticFilter")
    class MatchesStaticFilter {

        private FilterContext ctx() {
            return FilterContext.of(gd);
        }

        @Test
        @DisplayName("a null filter matches, because an absent scope filter means every permanent")
        void nullFilterMatches() {
            Permanent perm = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));

            assertThat(evaluator.matchesStaticFilter(perm, null, ctx())).isTrue();
        }

        @Test
        @DisplayName("a maximum mana value filter matches only permanents at or below its threshold")
        void maximumManaValueFilterMatches() {
            Card cheapCard = createCreature("Cheap Creature", 2, 2, CardColor.GREEN);
            cheapCard.setManaCost("{3}");
            Card expensiveCard = createCreature("Expensive Creature", 4, 4, CardColor.GREEN);
            expensiveCard.setManaCost("{4}");
            Permanent cheap = addPermanent(player1Id, cheapCard);
            Permanent expensive = addPermanent(player1Id, expensiveCard);

            PermanentMaxManaValuePredicate predicate = new PermanentMaxManaValuePredicate(3);

            assertThat(evaluator.matchesStaticFilter(cheap, predicate, ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(expensive, predicate, ctx())).isFalse();
        }

        @Test
        @DisplayName("composites propagate the recursion-safe evaluation to their operands")
        void compositesPropagate() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes(
                    "Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesStaticFilter(perm, new PermanentNotPredicate(new PermanentIsArtifactPredicate()), ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(perm, new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentHasSubtypePredicate(CardSubtype.BEAR))), ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(perm, new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentIsArtifactPredicate())), ctx())).isFalse();
            assertThat(evaluator.matchesStaticFilter(perm, new PermanentAnyOfPredicate(List.of(
                    new PermanentIsArtifactPredicate(),
                    new PermanentIsCreaturePredicate())), ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(perm, new PermanentAnyOfPredicate(List.of(
                    new PermanentIsArtifactPredicate(),
                    new PermanentIsLandPredicate())), ctx())).isFalse();
        }

        @Test
        @DisplayName("ownership predicates account for stolen permanents in static filters")
        void ownershipPredicateAccountsForStolenPermanents() {
            Permanent owned = addPermanent(player1Id, createCreature("Owned Creature", 2, 2, CardColor.GREEN));
            Permanent stolen = addPermanent(player1Id, createCreature("Stolen Creature", 2, 2, CardColor.GREEN));
            gd.stolenCreatures.put(stolen.getId(), player2Id);
            FilterContext sourceContext = ctx().withSourceControllerId(player1Id);

            PermanentOwnedBySourceControllerPredicate ownedPredicate =
                    new PermanentOwnedBySourceControllerPredicate();
            assertThat(evaluator.matchesStaticFilter(owned, ownedPredicate, sourceContext)).isTrue();
            assertThat(evaluator.matchesStaticFilter(stolen, ownedPredicate, sourceContext)).isFalse();
            assertThat(evaluator.matchesStaticFilter(stolen,
                    new PermanentNotPredicate(ownedPredicate), sourceContext)).isTrue();
        }

        @Test
        @DisplayName("controller predicate accounts for current permanent control")
        void controllerPredicateAccountsForCurrentControl() {
            Permanent controlled = addPermanent(player1Id,
                    createCreature("Controlled Creature", 2, 2, CardColor.GREEN));
            Permanent opponentControlled = addPermanent(player2Id,
                    createCreature("Opponent Controlled Creature", 2, 2, CardColor.GREEN));
            FilterContext sourceContext = ctx().withSourceControllerId(player1Id);

            PermanentControlledBySourceControllerPredicate controllerPredicate =
                    new PermanentControlledBySourceControllerPredicate();
            assertThat(evaluator.matchesStaticFilter(controlled, controllerPredicate, sourceContext)).isTrue();
            assertThat(evaluator.matchesStaticFilter(opponentControlled, controllerPredicate, sourceContext)).isFalse();
        }

        @Test
        @DisplayName("the source-permanent predicate uses the source snapshot or explicit source ID")
        void sourcePermanentUsesContextIdentity() {
            Permanent source = addPermanent(player1Id, createEnchantment("Sterling Grove"));
            Permanent other = addPermanent(player1Id, createEnchantment("Glorious Anthem"));
            PermanentIsSourcePermanentPredicate predicate = new PermanentIsSourcePermanentPredicate();

            FilterContext snapshotContext = ctx().withSourcePermanentSnapshot(source);
            assertThat(evaluator.matchesStaticFilter(source, predicate, snapshotContext)).isTrue();
            assertThat(evaluator.matchesStaticFilter(other, predicate, snapshotContext)).isFalse();

            FilterContext idContext = ctx().withSourcePermanentId(source.getId());
            assertThat(evaluator.matchesStaticFilter(source, predicate, idContext)).isTrue();
            assertThat(evaluator.matchesStaticFilter(other, predicate, idContext)).isFalse();
        }

        @Test
        @DisplayName("a predicate with no recursion-safe answer throws instead of silently not matching")
        void unsupportedPredicateThrows() {
            Permanent perm = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));

            assertThatThrownBy(() -> evaluator.matchesStaticFilter(perm, new PermanentIsMonocoloredPredicate(), ctx()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PermanentIsMonocoloredPredicate");
        }

        @Test
        @DisplayName("a persistently granted supertype counts, and a persistent removal blocks the printed one")
        void supertypeHonorsPersistentGrantsAndRemovals() {
            Card printedSnow = createLand("Snow-Covered Forest");
            printedSnow.setSupertypes(Set.of(CardSupertype.SNOW));
            Permanent removed = addPermanent(player1Id, printedSnow);
            removed.getPersistentRemovedSupertypes().add(CardSupertype.SNOW);

            Permanent granted = addPermanent(player1Id, createLand("Forest"));
            granted.getPersistentGrantedSupertypes().add(CardSupertype.SNOW);

            PermanentHasSupertypePredicate snow = new PermanentHasSupertypePredicate(CardSupertype.SNOW);
            assertThat(evaluator.matchesStaticFilter(removed, snow, ctx())).isFalse();
            assertThat(evaluator.matchesStaticFilter(granted, snow, ctx())).isTrue();
        }

        @Test
        @DisplayName("historic reads the effective legendary supertype and granted Saga, not only the printed line")
        void historicReadsGrantedLegendaryAndSaga() {
            Permanent artifact = addPermanent(player1Id, createArtifact("Angel's Feather"));
            Permanent plain = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            Permanent madeLegendary = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            madeLegendary.getPersistentGrantedSupertypes().add(CardSupertype.LEGENDARY);
            Permanent madeSaga = addPermanent(player1Id, createEnchantment("Chapter"));
            madeSaga.getGrantedSubtypes().add(CardSubtype.SAGA);

            PermanentIsHistoricPredicate historic = new PermanentIsHistoricPredicate();
            assertThat(evaluator.matchesStaticFilter(artifact, historic, ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(plain, historic, ctx())).isFalse();
            assertThat(evaluator.matchesStaticFilter(madeLegendary, historic, ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(madeSaga, historic, ctx())).isTrue();
        }

        @Test
        @DisplayName("is-enchanted reads the board from the context")
        void isEnchantedReadsTheBoard() {
            Permanent bare = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            Permanent enchanted = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            Permanent aura = addPermanent(player1Id,
                    createAura("Heart of Light", new PreventAllDamageToAndByEnchantedCreatureEffect()));
            aura.setAttachedTo(enchanted.getId());

            PermanentIsEnchantedPredicate isEnchanted = new PermanentIsEnchantedPredicate();
            assertThat(evaluator.matchesStaticFilter(enchanted, isEnchanted, ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(bare, isEnchanted, ctx())).isFalse();
        }

        @Test
        @DisplayName("host-of-source-aura matches the attachment host from the source snapshot")
        void hostOfSourceAuraReadsAttachmentFromSnapshot() {
            Permanent host = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            Permanent other = addPermanent(player1Id, createCreature("Runeclaw Bear", 2, 2, CardColor.GREEN));
            Permanent aura = addPermanent(player1Id,
                    createAura("Heart of Light", new PreventAllDamageToAndByEnchantedCreatureEffect()));
            aura.setAttachedTo(host.getId());

            FilterContext withAura = ctx().withSourcePermanentSnapshot(aura);
            PermanentIsHostOfSourceAuraPredicate isHost = new PermanentIsHostOfSourceAuraPredicate();
            assertThat(evaluator.matchesStaticFilter(host, isHost, withAura)).isTrue();
            assertThat(evaluator.matchesStaticFilter(other, isHost, withAura)).isFalse();
            assertThat(evaluator.matchesStaticFilter(host,
                    new PermanentNotPredicate(isHost), withAura)).isFalse();
            assertThat(evaluator.matchesStaticFilter(other,
                    new PermanentNotPredicate(isHost), withAura)).isTrue();

            FilterContext withLiveAura = ctx().withSourcePermanentId(aura.getId());
            assertThat(evaluator.matchesPermanentPredicate(host, isHost, withLiveAura)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(other, isHost, withLiveAura)).isFalse();
        }

        @Test
        @DisplayName("controller-controls looks at the target's own controller, not the source's")
        void controllerControlsUsesTheTargetsController() {
            Permanent lonely = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            Permanent accompanied = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            addPermanent(player1Id, createCreature("Runeclaw Bear", 2, 2, CardColor.GREEN));

            PermanentControllerControlsPermanentPredicate another =
                    new PermanentControllerControlsPermanentPredicate(new PermanentIsCreaturePredicate(), true);
            assertThat(evaluator.matchesStaticFilter(accompanied, another, ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(lonely, another, ctx())).isFalse();
        }

        @Test
        @DisplayName("greatest mana value compares printed mana values across both battlefields")
        void greatestManaValueSpansBothBattlefields() {
            Permanent small = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            Card wurm = createCreature("Craw Wurm", 6, 4, CardColor.GREEN);
            wurm.setManaCost("{4}{G}{G}");
            Permanent big = addPermanent(player2Id, wurm);

            PermanentHasGreatestManaValueAmongAllCreaturesPredicate greatest =
                    new PermanentHasGreatestManaValueAmongAllCreaturesPredicate();
            assertThat(evaluator.matchesStaticFilter(big, greatest, ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(small, greatest, ctx())).isFalse();
        }

        @Test
        @DisplayName("greatest mana value compares a controller's creatures and planeswalkers as one group")
        void greatestManaValueCombinesControllerCreaturesAndPlaneswalkers() {
            Permanent smallCreature = addPermanent(player2Id,
                    createCreature("Small Creature", 2, 2, CardColor.GREEN));
            Permanent planeswalker = addPermanent(player2Id,
                    createPlaneswalker("Planeswalker", "{4}"));
            addPermanent(player1Id, createCreature("Unrelated Large Creature", 6, 6, CardColor.RED));

            PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate greatest =
                    new PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate();
            assertThat(evaluator.matchesPermanentPredicate(gd, planeswalker, greatest)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, smallCreature, greatest)).isFalse();
            assertThat(evaluator.matchesStaticFilter(planeswalker, greatest, ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(smallCreature, greatest, ctx())).isFalse();
        }

        @Test
        @DisplayName("greatest artifact mana value compares artifacts across both battlefields")
        void greatestArtifactManaValueSpansBothBattlefields() {
            Card smallCard = createArtifact("Small Artifact");
            smallCard.setManaCost("{1}");
            Permanent small = addPermanent(player1Id, smallCard);
            Card bigCard = createArtifact("Big Artifact");
            bigCard.setManaCost("{3}");
            Permanent big = addPermanent(player2Id, bigCard);

            PermanentHasGreatestManaValueAmongAllArtifactsPredicate greatest =
                    new PermanentHasGreatestManaValueAmongAllArtifactsPredicate();
            assertThat(evaluator.matchesPermanentPredicate(gd, big, greatest)).isTrue();
            assertThat(evaluator.matchesPermanentPredicate(gd, small, greatest)).isFalse();
            assertThat(evaluator.matchesStaticFilter(big, greatest, ctx())).isTrue();
            assertThat(evaluator.matchesStaticFilter(small, greatest, ctx())).isFalse();
        }

        @Test
        @DisplayName("source-chosen subtype resolves the source through the context")
        void sourceChosenSubtypeResolvesFromContext() {
            Permanent bear = addPermanent(player1Id, createCreatureWithSubtypes(
                    "Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Card sourceCard = createCreature("Shimmer Source", 1, 1, CardColor.BLUE);
            Permanent source = addPermanent(player1Id, sourceCard);
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(sourceCard.getId());

            PermanentHasSourceChosenSubtypePredicate chosen = new PermanentHasSourceChosenSubtypePredicate();
            assertThat(evaluator.matchesStaticFilter(bear, chosen, ctx)).isFalse();

            source.setChosenSubtype(CardSubtype.BEAR);
            assertThat(evaluator.matchesStaticFilter(bear, chosen, ctx)).isTrue();

            source.setChosenSubtype(CardSubtype.GOBLIN);
            assertThat(evaluator.matchesStaticFilter(bear, chosen, ctx)).isFalse();
        }
    }

    @Nested
    @DisplayName("PermanentBlockedBySourcePredicate")
    class BlockedBySource {

        private Permanent attacker;
        private Permanent blocker;

        @BeforeEach
        void setUpCombat() {
            attacker = new Permanent(createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            gd.playerBattlefields.get(player1Id).add(attacker);
            attacker.setAttacking(true);

            blocker = new Permanent(createCreature("Wall", 0, 3, CardColor.GREEN));
            gd.playerBattlefields.get(player2Id).add(blocker);
            blocker.setBlocking(true);
            blocker.getBlockingTargetIds().add(attacker.getId());
        }

        @Test
        @DisplayName("Matches the creature the source is blocking")
        void matchesBlockedCreature() {
            FilterContext context = FilterContext.of(gd)
                    .withSourceCardId(blocker.getOriginalCard().getId())
                    .withSourceControllerId(player2Id);

            assertThat(evaluator.matchesPermanentPredicate(attacker, new PermanentBlockedBySourcePredicate(), context))
                    .isTrue();
        }

        @Test
        @DisplayName("Uses the source's last known information once it has left the battlefield (CR 608.2b)")
        void usesLastKnownInformationForSacrificedSource() {
            gd.playerBattlefields.get(player2Id).remove(blocker);

            FilterContext context = FilterContext.of(gd)
                    .withSourceCardId(blocker.getOriginalCard().getId())
                    .withSourceControllerId(player2Id)
                    .withSourcePermanentSnapshot(blocker);

            assertThat(evaluator.matchesPermanentPredicate(attacker, new PermanentBlockedBySourcePredicate(), context))
                    .isTrue();
        }

        @Test
        @DisplayName("Does not match without the source on the battlefield and no snapshot")
        void noMatchWithoutSourceOrSnapshot() {
            gd.playerBattlefields.get(player2Id).remove(blocker);

            FilterContext context = FilterContext.of(gd)
                    .withSourceCardId(blocker.getOriginalCard().getId())
                    .withSourceControllerId(player2Id);

            assertThat(evaluator.matchesPermanentPredicate(attacker, new PermanentBlockedBySourcePredicate(), context))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("PermanentBlockingSourcePredicate")
    class BlockingSource {

        private Permanent attacker;
        private Permanent blocker;

        @BeforeEach
        void setUpCombat() {
            attacker = new Permanent(createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            gd.playerBattlefields.get(player1Id).add(attacker);
            attacker.setAttacking(true);

            blocker = new Permanent(createCreature("Wall", 0, 3, CardColor.GREEN));
            gd.playerBattlefields.get(player2Id).add(blocker);
            blocker.setBlocking(true);
            blocker.getBlockingTargetIds().add(attacker.getId());
        }

        @Test
        @DisplayName("Matches a creature blocking the source")
        void matchesBlockerOfSource() {
            FilterContext context = FilterContext.of(gd)
                    .withSourceCardId(attacker.getOriginalCard().getId())
                    .withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(blocker, new PermanentBlockingSourcePredicate(), context))
                    .isTrue();
        }

        @Test
        @DisplayName("Does not match the creature the source is blocking (opposite direction)")
        void doesNotMatchBlockedCreature() {
            FilterContext context = FilterContext.of(gd)
                    .withSourceCardId(blocker.getOriginalCard().getId())
                    .withSourceControllerId(player2Id);

            assertThat(evaluator.matchesPermanentPredicate(attacker, new PermanentBlockingSourcePredicate(), context))
                    .isFalse();
        }

        @Test
        @DisplayName("Does not match a creature blocking something else")
        void doesNotMatchBlockerOfAnotherCreature() {
            Permanent otherAttacker = new Permanent(createCreature("Hill Giant", 3, 3, CardColor.RED));
            gd.playerBattlefields.get(player1Id).add(otherAttacker);
            otherAttacker.setAttacking(true);

            FilterContext context = FilterContext.of(gd)
                    .withSourceCardId(otherAttacker.getOriginalCard().getId())
                    .withSourceControllerId(player1Id);

            assertThat(evaluator.matchesPermanentPredicate(blocker, new PermanentBlockingSourcePredicate(), context))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("PermanentInCombatWithSourcePredicate")
    class InCombatWithSource {

        @Test
        @DisplayName("Matches a creature blocking a source that has left the battlefield")
        void matchesAgainstSourceSnapshot() {
            Permanent attacker = addPermanent(player1Id, createCreature("Attacker", 0, 1, CardColor.WHITE));
            attacker.setAttacking(true);
            Permanent blocker = addPermanent(player2Id, createCreature("Blocker", 2, 2, CardColor.GREEN));
            blocker.setBlocking(true);
            blocker.getBlockingTargetIds().add(attacker.getId());

            Permanent sourceSnapshot = new Permanent(attacker);
            gd.combatBlockOpponentIdsThisCombat.put(blocker.getId(), Set.of(attacker.getId()));
            blocker.setBlocking(false);
            blocker.getBlockingTargetIds().clear();
            gd.playerBattlefields.get(player1Id).remove(attacker);

            FilterContext context = FilterContext.of(gd)
                    .withSourceCardId(attacker.getOriginalCard().getId())
                    .withSourcePermanentSnapshot(sourceSnapshot);

            assertThat(evaluator.matchesPermanentPredicate(
                    blocker, new PermanentInCombatWithSourcePredicate(), context)).isTrue();
        }
    }

    @Nested
    @DisplayName("PermanentBlockedBySourceThisTurnPredicate")
    class BlockedBySourceThisTurn {

        @Test
        @DisplayName("Matches an attacker blocked by the source after combat state is cleared")
        void matchesAttackerBlockedBySourceAfterCombat() {
            Permanent attacker = addPermanent(player1Id, createCreature("Attacker", 2, 2, CardColor.GREEN));
            Permanent blocker = addPermanent(player2Id, createCreature("Wall", 0, 7, CardColor.WHITE));

            gd.creaturesBlockedThisTurn.add(attacker.getId());
            gd.combatBlockOpponentIdsThisTurn
                    .computeIfAbsent(attacker.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(blocker.getId());

            FilterContext context = FilterContext.of(gd)
                    .withSourceCardId(blocker.getOriginalCard().getId())
                    .withSourcePermanentId(blocker.getId());

            assertThat(evaluator.matchesPermanentPredicate(
                    attacker, new PermanentBlockedBySourceThisTurnPredicate(), context)).isTrue();
        }

        @Test
        @DisplayName("Does not match a creature that only blocked the source")
        void doesNotMatchCreatureBlockingSource() {
            Permanent attacker = addPermanent(player1Id, createCreature("Attacker", 2, 2, CardColor.GREEN));
            Permanent blocker = addPermanent(player2Id, createCreature("Wall", 0, 7, CardColor.WHITE));

            gd.combatBlockOpponentIdsThisTurn
                    .computeIfAbsent(blocker.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(attacker.getId());

            FilterContext context = FilterContext.of(gd)
                    .withSourceCardId(blocker.getOriginalCard().getId())
                    .withSourcePermanentId(blocker.getId());

            assertThat(evaluator.matchesPermanentPredicate(
                    attacker, new PermanentBlockedBySourceThisTurnPredicate(), context)).isFalse();
        }
    }

}
