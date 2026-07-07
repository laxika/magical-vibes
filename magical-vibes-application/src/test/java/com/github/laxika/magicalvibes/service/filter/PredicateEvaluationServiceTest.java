package com.github.laxika.magicalvibes.service.filter;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
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
        @DisplayName("null predicate returns true")
        void nullPredicateReturnsTrue() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(evaluator.matchesCardPredicate(card, null, null)).isTrue();
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
        @DisplayName("CardSubtypePredicate matches subtype")
        void cardSubtypePredicateMatches() {
            Card card = createCreature("Elf", 1, 1, CardColor.GREEN);
            card.setSubtypes(List.of(CardSubtype.ELF));

            assertThat(evaluator.matchesCardPredicate(card, new CardSubtypePredicate(CardSubtype.ELF), null)).isTrue();
            assertThat(evaluator.matchesCardPredicate(card, new CardSubtypePredicate(CardSubtype.GOBLIN), null)).isFalse();
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
        @DisplayName("PermanentIsPlaneswalkerPredicate rejects non-planeswalker")
        void planeswalkerPredicateRejectsNonPlaneswalker() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(evaluator.matchesPermanentPredicate(gd, perm, new PermanentIsPlaneswalkerPredicate())).isFalse();
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

}
