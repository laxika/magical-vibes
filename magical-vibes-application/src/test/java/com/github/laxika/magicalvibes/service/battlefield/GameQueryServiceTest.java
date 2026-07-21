package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CountersCantBePlacedEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantGetPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCreatureSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerShroudEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromEverythingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.model.CounterType;

@ExtendWith(MockitoExtension.class)
class GameQueryServiceTest {

    @Mock
    private StaticEffectHandlerRegistry staticEffectRegistry;

    @Mock
    private ConditionEvaluationService conditionEvaluationService;

    @InjectMocks
    private GameQueryService gqs;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        PredicateEvaluationService evaluator = new PredicateEvaluationService(gqs);
        ReflectionTestUtils.setField(gqs, "predicateEvaluationService", evaluator);
        LayerSystemService layerSystemService = new LayerSystemService();
        ReflectionTestUtils.setField(layerSystemService, "predicateEvaluationService", evaluator);
        ReflectionTestUtils.setField(layerSystemService, "staticEffectRegistry", staticEffectRegistry);
        ReflectionTestUtils.setField(layerSystemService, "gameQueryService", gqs);
        ReflectionTestUtils.setField(gqs, "layerSystemService", layerSystemService);
        ReflectionTestUtils.setField(gqs, "conditionEvaluationService", conditionEvaluationService);

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
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createCreatureWithSubtypes(String name, int power, int toughness, CardColor color, List<CardSubtype> subtypes) {
        Card card = createCreature(name, power, toughness, color);
        card.setSubtypes(subtypes);
        return card;
    }

    private static Card createCreatureWithKeywords(String name, int power, int toughness, CardColor color, Set<Keyword> keywords) {
        Card card = createCreature(name, power, toughness, color);
        card.setKeywords(keywords);
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

    private static Card createPlaneswalker(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.PLANESWALKER);
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

    private static Card createCreatureWithStaticEffect(String name, int power, int toughness, CardColor color, CardEffect effect) {
        Card card = createCreature(name, power, toughness, color);
        card.addEffect(EffectSlot.STATIC, effect);
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

    /**
     * Creates a "lord" permanent on the given player's battlefield whose static effect
     * is handled by the provided handler. Uses {@link DoubleDamageEffect} as a carrier
     * effect — the actual behavior is defined entirely by the handler lambda.
     */
    private Permanent addLordWithHandler(UUID playerId,
                                         com.github.laxika.magicalvibes.service.effect.StaticEffectHandler handler) {
        CardEffect carrierEffect = new DoubleDamageEffect();
        Card lordCard = createCreature("Test Lord", 1, 1, null);
        lordCard.addEffect(EffectSlot.STATIC, carrierEffect);
        when(staticEffectRegistry.getHandler(carrierEffect)).thenReturn(handler);
        return addPermanent(playerId, lordCard);
    }

    // ===== findPermanentById =====

    @Nested
    @DisplayName("findPermanentById")
    class FindPermanentById {

        @Test
        @DisplayName("finds permanent on player1's battlefield")
        void findOnPlayer1Battlefield() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.findPermanentById(gd, perm.getId())).isSameAs(perm);
        }

        @Test
        @DisplayName("finds permanent on player2's battlefield")
        void findOnPlayer2Battlefield() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.findPermanentById(gd, perm.getId())).isSameAs(perm);
        }

        @Test
        @DisplayName("returns null for null id")
        void returnsNullForNullId() {
            assertThat(gqs.findPermanentById(gd, null)).isNull();
        }

        @Test
        @DisplayName("returns null when permanent not found")
        void returnsNullWhenNotFound() {
            assertThat(gqs.findPermanentById(gd, UUID.randomUUID())).isNull();
        }
    }

    // ===== findPermanentController =====

    @Nested
    @DisplayName("findPermanentController")
    class FindPermanentController {

        @Test
        @DisplayName("returns player1 for permanent on player1's battlefield")
        void returnsPlayer1() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.findPermanentController(gd, perm.getId())).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("returns player2 for permanent on player2's battlefield")
        void returnsPlayer2() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.findPermanentController(gd, perm.getId())).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("returns null for null id")
        void returnsNullForNullId() {
            assertThat(gqs.findPermanentController(gd, null)).isNull();
        }

        @Test
        @DisplayName("returns null when permanent not found")
        void returnsNullWhenNotFound() {
            assertThat(gqs.findPermanentController(gd, UUID.randomUUID())).isNull();
        }
    }

    // ===== findCardInGraveyardById =====

    @Nested
    @DisplayName("findCardInGraveyardById")
    class FindCardInGraveyardById {

        @Test
        @DisplayName("finds card in player1's graveyard")
        void findInPlayer1Graveyard() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));
            gd.playerGraveyards.get(player1Id).add(card);

            assertThat(gqs.findCardInGraveyardById(gd, card.getId())).isSameAs(card);
        }

        @Test
        @DisplayName("finds card in player2's graveyard")
        void findInPlayer2Graveyard() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));
            gd.playerGraveyards.get(player2Id).add(card);

            assertThat(gqs.findCardInGraveyardById(gd, card.getId())).isSameAs(card);
        }

        @Test
        @DisplayName("returns null for null id")
        void returnsNullForNullId() {
            assertThat(gqs.findCardInGraveyardById(gd, null)).isNull();
        }

        @Test
        @DisplayName("returns null when card not found")
        void returnsNullWhenNotFound() {
            assertThat(gqs.findCardInGraveyardById(gd, UUID.randomUUID())).isNull();
        }
    }

    // ===== findGraveyardOwnerById =====

    @Nested
    @DisplayName("findGraveyardOwnerById")
    class FindGraveyardOwnerById {

        @Test
        @DisplayName("returns player1 for card in player1's graveyard")
        void returnsPlayer1() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));
            gd.playerGraveyards.get(player1Id).add(card);

            assertThat(gqs.findGraveyardOwnerById(gd, card.getId())).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("returns player2 for card in player2's graveyard")
        void returnsPlayer2() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));
            gd.playerGraveyards.get(player2Id).add(card);

            assertThat(gqs.findGraveyardOwnerById(gd, card.getId())).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("returns null for null id")
        void returnsNullForNullId() {
            assertThat(gqs.findGraveyardOwnerById(gd, null)).isNull();
        }

        @Test
        @DisplayName("returns null when card not found")
        void returnsNullWhenNotFound() {
            assertThat(gqs.findGraveyardOwnerById(gd, UUID.randomUUID())).isNull();
        }
    }

    // ===== getOpponentId =====

    @Nested
    @DisplayName("getOpponentId")
    class GetOpponentId {

        @Test
        @DisplayName("returns player2 when given player1")
        void returnsPlayer2ForPlayer1() {
            assertThat(gqs.getOpponentId(gd, player1Id)).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("returns player1 when given player2")
        void returnsPlayer1ForPlayer2() {
            assertThat(gqs.getOpponentId(gd, player2Id)).isEqualTo(player1Id);
        }
    }

    // ===== getPriorityPlayerId =====

    @Nested
    @DisplayName("getPriorityPlayerId")
    class GetPriorityPlayerId {

        @Test
        @DisplayName("returns active player when neither has passed")
        void returnsActivePlayer() {
            gd.activePlayerId = player1Id;
            gd.priorityPassedBy.clear();

            assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("returns non-active player when active has passed")
        void returnsNonActiveWhenActivePassed() {
            gd.activePlayerId = player1Id;
            gd.priorityPassedBy.clear();
            gd.priorityPassedBy.add(player1Id);

            assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("returns null when both have passed")
        void returnsNullWhenBothPassed() {
            gd.activePlayerId = player1Id;
            gd.priorityPassedBy.clear();
            gd.priorityPassedBy.add(player1Id);
            gd.priorityPassedBy.add(player2Id);

            assertThat(gqs.getPriorityPlayerId(gd)).isNull();
        }

        @Test
        @DisplayName("returns null when activePlayerId is null")
        void returnsNullWhenNoActivePlayer() {
            gd.activePlayerId = null;

            assertThat(gqs.getPriorityPlayerId(gd)).isNull();
        }
    }

    // ===== canPlayerLifeChange =====

    @Nested
    @DisplayName("canPlayerLifeChange")
    class CanPlayerLifeChange {

        @Test
        @DisplayName("returns true by default")
        void returnsTrueByDefault() {
            assertThat(gqs.canPlayerLifeChange(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("returns false when PlatinumEmperion is on battlefield")
        void returnsFalseWithPlatinumEmperion() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Platinum Emperion", 8, 8, null, new LifeTotalCantChangeEffect()));

            assertThat(gqs.canPlayerLifeChange(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("only affects the controller, not the opponent")
        void onlyAffectsController() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Platinum Emperion", 8, 8, null, new LifeTotalCantChangeEffect()));

            assertThat(gqs.canPlayerLifeChange(gd, player2Id)).isTrue();
        }
    }

    // ===== canPlayerLoseGame =====

    @Nested
    @DisplayName("canPlayerLoseGame")
    class CanPlayerLoseGame {

        @Test
        @DisplayName("returns true by default")
        void returnsTrueByDefault() {
            assertThat(gqs.canPlayerLoseGame(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("returns false when PlatinumAngel is on battlefield")
        void returnsFalseWithPlatinumAngel() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Platinum Angel", 4, 4, null, new CantLoseGameEffect()));

            assertThat(gqs.canPlayerLoseGame(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("only affects the controller, not the opponent")
        void onlyAffectsController() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Platinum Angel", 4, 4, null, new CantLoseGameEffect()));

            assertThat(gqs.canPlayerLoseGame(gd, player2Id)).isTrue();
        }
    }

    // ===== isCreature =====

    @Nested
    @DisplayName("isCreature")
    class IsCreature {

        @Test
        @DisplayName("returns true for a creature")
        void returnsTrueForCreature() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.isCreature(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns false for a non-creature")
        void returnsFalseForNonCreature() {
            Permanent perm = addPermanent(player1Id, createLand("Forest"));

            assertThat(gqs.isCreature(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true for animated permanent")
        void returnsTrueForAnimated() {
            Permanent perm = addPermanent(player1Id, createLand("Forest"));
            perm.setAnimatedUntilEndOfTurn(true);
            perm.setAnimatedPower(2);
            perm.setAnimatedToughness(2);

            assertThat(gqs.isCreature(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true for permanent with awakening counters")
        void returnsTrueForAwakened() {
            Permanent perm = addPermanent(player1Id, createLand("Forest"));
            perm.setCounterCount(CounterType.AWAKENING, 1);

            assertThat(gqs.isCreature(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true for artifact with animate artifact effect on battlefield")
        void returnsTrueForArtifactWithAnimateEffect() {
            addPermanent(player1Id, createEnchantmentWithStaticEffect("March of the Machines", new AnimateNoncreatureArtifactsEffect()));
            Permanent artifact = addPermanent(player1Id, createArtifact("Angel's Feather"));

            assertThat(gqs.isCreature(gd, artifact)).isTrue();
        }

        @Test
        @DisplayName("returns true for artifact creature")
        void returnsTrueForArtifactCreature() {
            Permanent perm = addPermanent(player1Id, createArtifactCreature("Myr Sire", 1, 1, List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR)));

            assertThat(gqs.isCreature(gd, perm)).isTrue();
        }
    }

    // ===== isArtifact =====

    @Nested
    @DisplayName("isArtifact")
    class IsArtifact {

        @Test
        @DisplayName("returns true for artifact card type")
        void returnsTrueForArtifact() {
            Permanent perm = addPermanent(player1Id, createArtifact("Angel's Feather"));

            assertThat(gqs.isArtifact(perm)).isTrue();
        }

        @Test
        @DisplayName("returns false for non-artifact")
        void returnsFalseForNonArtifact() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.isArtifact(perm)).isFalse();
        }

        @Test
        @DisplayName("returns true for artifact creature (additional type)")
        void returnsTrueForArtifactCreature() {
            Permanent perm = addPermanent(player1Id, createArtifactCreature("Myr Sire", 1, 1, List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR)));

            assertThat(gqs.isArtifact(perm)).isTrue();
        }

        @Test
        @DisplayName("returns true for permanent with granted artifact type")
        void returnsTrueForGrantedType() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.getGrantedCardTypes().add(CardType.ARTIFACT);

            assertThat(gqs.isArtifact(perm)).isTrue();
        }
    }

    // ===== isArtifact (with GameData) =====

    @Nested
    @DisplayName("isArtifact (with GameData)")
    class IsArtifactWithGameData {

        @Test
        @DisplayName("returns true when artifact type granted by static effect from another permanent")
        void returnsTrueWhenArtifactTypeGrantedByStaticEffect() {
            addLordWithHandler(player1Id,
                    (ctx, eff, acc) -> acc.addGrantedCardType(CardType.ARTIFACT));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.isArtifact(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns false when no artifact type granted by static effect")
        void returnsFalseWithoutStaticGrant() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.isArtifact(gd, perm)).isFalse();
        }
    }

    // ===== isMetalcraftMet =====

    @Nested
    @DisplayName("isMetalcraftMet")
    class IsMetalcraftMet {

        @Test
        @DisplayName("returns false with no artifacts")
        void returnsFalseWithNoArtifacts() {
            assertThat(gqs.isMetalcraftMet(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("returns false with 2 artifacts")
        void returnsFalseWithTwoArtifacts() {
            addPermanent(player1Id, createArtifact("Artifact A"));
            addPermanent(player1Id, createArtifact("Artifact B"));

            assertThat(gqs.isMetalcraftMet(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("returns true with exactly 3 artifacts")
        void returnsTrueWithThreeArtifacts() {
            addPermanent(player1Id, createArtifact("Artifact A"));
            addPermanent(player1Id, createArtifact("Artifact B"));
            addPermanent(player1Id, createArtifact("Artifact C"));

            assertThat(gqs.isMetalcraftMet(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("returns true with more than 3 artifacts")
        void returnsTrueWithMoreThanThree() {
            addPermanent(player1Id, createArtifact("Artifact A"));
            addPermanent(player1Id, createArtifact("Artifact B"));
            addPermanent(player1Id, createArtifact("Artifact C"));
            addPermanent(player1Id, createArtifact("Artifact D"));

            assertThat(gqs.isMetalcraftMet(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("does not count opponent's artifacts")
        void doesNotCountOpponentArtifacts() {
            addPermanent(player2Id, createArtifact("Artifact A"));
            addPermanent(player2Id, createArtifact("Artifact B"));
            addPermanent(player2Id, createArtifact("Artifact C"));

            assertThat(gqs.isMetalcraftMet(gd, player1Id)).isFalse();
        }
    }

    // ===== hasKeyword =====

    @Nested
    @DisplayName("hasKeyword")
    class HasKeyword {

        @Test
        @DisplayName("returns true for innate keyword")
        void returnsTrueForInnateKeyword() {
            Permanent perm = addPermanent(player1Id, createMirranCrusader());

            assertThat(gqs.hasKeyword(gd, perm, Keyword.DOUBLE_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("returns false when keyword not present")
        void returnsFalseWhenAbsent() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.hasKeyword(gd, perm, Keyword.FLYING)).isFalse();
        }

        @Test
        @DisplayName("returns true for granted keyword")
        void returnsTrueForGrantedKeyword() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.getGrantedKeywords().add(Keyword.FLYING);

            assertThat(gqs.hasKeyword(gd, perm, Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("returns true when keyword granted by static effect from another permanent")
        void returnsTrueForKeywordGrantedByStaticEffect() {
            addLordWithHandler(player1Id, (ctx, eff, acc) -> acc.addKeyword(Keyword.FLYING));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.hasKeyword(gd, perm, Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("returns false when innate keyword is removed by static effect")
        void returnsFalseWhenKeywordRemovedByStaticEffect() {
            addLordWithHandler(player1Id, (ctx, eff, acc) -> acc.removeKeyword(Keyword.DOUBLE_STRIKE));
            Permanent perm = addPermanent(player1Id, createMirranCrusader());

            assertThat(gqs.hasKeyword(gd, perm, Keyword.DOUBLE_STRIKE)).isFalse();
        }
    }

    // ===== hasKeyword (pre-computed bonus overload) =====

    @Nested
    @DisplayName("hasKeyword with pre-computed StaticBonus")
    class HasKeywordWithBonus {

        @Test
        @DisplayName("matches the GameData overload for innate keywords (NONE bonus)")
        void matchesForInnateKeyword() {
            Permanent perm = addPermanent(player1Id, createMirranCrusader());
            GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, perm);

            assertThat(gqs.hasKeyword(perm, bonus, Keyword.DOUBLE_STRIKE)).isTrue();
            assertThat(gqs.hasKeyword(perm, bonus, Keyword.FLYING)).isFalse();
        }

        @Test
        @DisplayName("matches the GameData overload for statically granted keywords")
        void matchesForGrantedKeyword() {
            addLordWithHandler(player1Id, (ctx, eff, acc) -> acc.addKeyword(Keyword.FLYING));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, perm);

            assertThat(gqs.hasKeyword(perm, bonus, Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("matches the GameData overload for removed keywords")
        void matchesForRemovedKeyword() {
            addLordWithHandler(player1Id, (ctx, eff, acc) -> acc.removeKeyword(Keyword.DOUBLE_STRIKE));
            Permanent perm = addPermanent(player1Id, createMirranCrusader());
            GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, perm);

            assertThat(gqs.hasKeyword(perm, bonus, Keyword.DOUBLE_STRIKE)).isFalse();
        }
    }

    // ===== withQueryScope =====

    @Nested
    @DisplayName("withQueryScope")
    class WithQueryScope {

        @Test
        @DisplayName("queries inside the scope return the same results as outside")
        void sameResultsInsideScope() {
            addLordWithHandler(player1Id, (ctx, eff, acc) -> {
                acc.addPower(2);
                acc.addKeyword(Keyword.FLYING);
            });
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            int powerOutside = gqs.getEffectivePower(gd, perm);
            boolean flyingOutside = gqs.hasKeyword(gd, perm, Keyword.FLYING);

            int[] powerInside = new int[1];
            boolean[] flyingInside = new boolean[1];
            String result = gqs.withQueryScope(gd, () -> {
                // Repeated queries exercise the pass-level bonus memo
                powerInside[0] = gqs.getEffectivePower(gd, perm);
                flyingInside[0] = gqs.hasKeyword(gd, perm, Keyword.FLYING);
                gqs.getEffectiveToughness(gd, perm);
                return "done";
            });

            assertThat(result).isEqualTo("done");
            assertThat(powerInside[0]).isEqualTo(powerOutside).isEqualTo(4);
            assertThat(flyingInside[0]).isEqualTo(flyingOutside).isTrue();
        }

        @Test
        @DisplayName("nested scopes reuse the active pass")
        void nestedScopesReuseActivePass() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            int power = gqs.withQueryScope(gd,
                    () -> gqs.withQueryScope(gd, () -> gqs.getEffectivePower(gd, perm)));

            assertThat(power).isEqualTo(2);
        }

        @Test
        @DisplayName("queries after the scope see state changes made after it ends")
        void queriesAfterScopeSeeFreshState() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            gqs.withQueryScope(gd, () -> gqs.getEffectivePower(gd, perm));
            perm.setPowerModifier(3);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        }
    }

    // ===== cantHaveCounters =====

    @Nested
    @DisplayName("cantHaveCounters")
    class CantHaveCounters {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.cantHaveCounters(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true for permanent with CantHaveCountersEffect")
        void returnsTrueWithEffect() {
            Permanent perm = addPermanent(player1Id, createCreatureWithStaticEffect("Melira's Keepers", 2, 2, CardColor.GREEN, new CantHaveCountersEffect()));

            assertThat(gqs.cantHaveCounters(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true when CantHaveCountersEffect granted by static effect from another permanent")
        void returnsTrueWhenGrantedByStaticEffect() {
            addLordWithHandler(player1Id,
                    (ctx, eff, acc) -> acc.addGrantedEffect(new CantHaveCountersEffect()));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.cantHaveCounters(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("Solemnity blocks a creature — the lock is global (any battlefield)")
        void solemnityBlocksCreature() {
            addPermanent(player2Id, createEnchantmentWithStaticEffect("Solemnity", new CountersCantBePlacedEffect()));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.cantHaveCounters(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("Solemnity blocks an artifact")
        void solemnityBlocksArtifact() {
            addPermanent(player1Id, createEnchantmentWithStaticEffect("Solemnity", new CountersCantBePlacedEffect()));
            Permanent perm = addPermanent(player1Id, createArtifact("Angel's Feather"));

            assertThat(gqs.cantHaveCounters(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("Solemnity does not block a planeswalker (loyalty still allowed)")
        void solemnityDoesNotBlockPlaneswalker() {
            addPermanent(player1Id, createEnchantmentWithStaticEffect("Solemnity", new CountersCantBePlacedEffect()));
            Permanent perm = addPermanent(player1Id, createPlaneswalker("Some Planeswalker"));

            assertThat(gqs.cantHaveCounters(gd, perm)).isFalse();
        }
    }

    // ===== canPlayerGetPoisonCounters =====

    @Nested
    @DisplayName("canPlayerGetPoisonCounters")
    class CanPlayerGetPoisonCounters {

        @Test
        @DisplayName("returns true by default")
        void returnsTrueByDefault() {
            assertThat(gqs.canPlayerGetPoisonCounters(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("returns false only for the controller of a PlayerCantGetPoisonCountersEffect permanent")
        void returnsFalseWithCantGetPoisonEffect() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Melira", 2, 2, CardColor.GREEN, new PlayerCantGetPoisonCountersEffect()));

            assertThat(gqs.canPlayerGetPoisonCounters(gd, player1Id)).isFalse();
            assertThat(gqs.canPlayerGetPoisonCounters(gd, player2Id)).isTrue();
        }

        @Test
        @DisplayName("Solemnity: no player can get poison counters (global lock)")
        void solemnityBlocksAllPlayers() {
            addPermanent(player1Id, createEnchantmentWithStaticEffect("Solemnity", new CountersCantBePlacedEffect()));

            assertThat(gqs.canPlayerGetPoisonCounters(gd, player1Id)).isFalse();
            assertThat(gqs.canPlayerGetPoisonCounters(gd, player2Id)).isFalse();
        }
    }

    // ===== cantHaveMinusOneMinusOneCounters =====

    @Nested
    @DisplayName("cantHaveMinusOneMinusOneCounters")
    class CantHaveMinusOneMinusOneCountersTest {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true when granted by static effect from another permanent")
        void returnsTrueWhenGrantedByStaticEffect() {
            addLordWithHandler(player1Id,
                    (ctx, eff, acc) -> acc.addGrantedEffect(new CantHaveMinusOneMinusOneCountersEffect()));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.cantHaveMinusOneMinusOneCounters(gd, perm)).isTrue();
        }
    }

    // ===== hasCantBeBlocked =====

    @Nested
    @DisplayName("hasCantBeBlocked")
    class HasCantBeBlocked {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.hasCantBeBlocked(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true when permanent cantBeBlocked flag is set")
        void returnsTrueWhenFlagSet() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setCantBeBlocked(true);

            assertThat(gqs.hasCantBeBlocked(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true when card has CantBeBlockedEffect")
        void returnsTrueWithStaticEffect() {
            Permanent perm = addPermanent(player1Id, createCreatureWithStaticEffect("Phantom Warrior", 2, 2, CardColor.BLUE, new CantBeBlockedEffect()));

            assertThat(gqs.hasCantBeBlocked(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true when attached equipment has CantBeBlockedEffect")
        void returnsTrueWithAttachedEquipment() {
            Permanent creature = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            Card equipCard = createArtifact("Unblockable Cloak");
            equipCard.addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
            Permanent equipment = addPermanent(player1Id, equipCard);
            equipment.setAttachedTo(creature.getId());

            assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
        }

        @Test
        @DisplayName("returns true when CantBeBlockedEffect granted by static effect from another permanent")
        void returnsTrueWhenGrantedByStaticEffect() {
            addLordWithHandler(player1Id,
                    (ctx, eff, acc) -> acc.addGrantedEffect(new CantBeBlockedEffect()));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.hasCantBeBlocked(gd, perm)).isTrue();
        }
    }

    // ===== isLethalDamage =====

    @Nested
    @DisplayName("isLethalDamage")
    class IsLethalDamage {

        @Test
        @DisplayName("returns true when damage >= toughness")
        void lethalWhenDamageEqualsToughness() {
            assertThat(gqs.isLethalDamage(3, 3, false)).isTrue();
        }

        @Test
        @DisplayName("returns true when damage exceeds toughness")
        void lethalWhenDamageExceedsToughness() {
            assertThat(gqs.isLethalDamage(5, 3, false)).isTrue();
        }

        @Test
        @DisplayName("returns false when damage less than toughness")
        void nonLethalWhenDamageBelowToughness() {
            assertThat(gqs.isLethalDamage(2, 3, false)).isFalse();
        }

        @Test
        @DisplayName("returns true with deathtouch when damage >= 1")
        void lethalWithDeathtouch() {
            assertThat(gqs.isLethalDamage(1, 5, true)).isTrue();
        }

        @Test
        @DisplayName("returns false with deathtouch when damage is 0")
        void nonLethalWithDeathtouchZeroDamage() {
            assertThat(gqs.isLethalDamage(0, 5, true)).isFalse();
        }
    }

    // ===== getEffectivePower / getEffectiveToughness =====

    @Nested
    @DisplayName("getEffectivePower and getEffectiveToughness")
    class EffectivePowerToughness {

        @Test
        @DisplayName("returns base power for vanilla creature")
        void basePower() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        }

        @Test
        @DisplayName("returns base toughness for vanilla creature")
        void baseToughness() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
        }

        @Test
        @DisplayName("includes power modifier")
        void includesPowerModifier() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setPowerModifier(3);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        }

        @Test
        @DisplayName("includes toughness modifier")
        void includesToughnessModifier() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setToughnessModifier(2);

            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
        }

        @Test
        @DisplayName("includes +1/+1 counters")
        void includesPlusCounters() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
        }

        @Test
        @DisplayName("includes -1/-1 counters")
        void includesMinusCounters() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
        }

        @Test
        @DisplayName("includes power from static bonus of another permanent")
        void includesStaticBonusPower() {
            addLordWithHandler(player1Id, (ctx, eff, acc) -> acc.addPower(2));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
        }

        @Test
        @DisplayName("includes toughness from static bonus of another permanent")
        void includesStaticBonusToughness() {
            addLordWithHandler(player1Id, (ctx, eff, acc) -> acc.addToughness(1));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
        }
    }

    // ===== getEffectiveCombatDamage =====

    @Nested
    @DisplayName("getEffectiveCombatDamage")
    class GetEffectiveCombatDamage {

        @Test
        @DisplayName("returns power normally")
        void returnsPowerNormally() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.getEffectiveCombatDamage(gd, perm)).isEqualTo(2);
        }

        @Test
        @DisplayName("clamps to 0 when power is negative (CR 510.1a)")
        void clampsNegativePowerToZero() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 5);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(-3);
            assertThat(gqs.getEffectiveCombatDamage(gd, perm)).isEqualTo(0);
        }
    }

    // ===== getPowerBasedDamage =====

    @Nested
    @DisplayName("getPowerBasedDamage")
    class GetPowerBasedDamage {

        @Test
        @DisplayName("returns power normally")
        void returnsPowerNormally() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.getPowerBasedDamage(gd, perm)).isEqualTo(2);
        }

        @Test
        @DisplayName("clamps to 0 when power is negative")
        void clampsNegativePowerToZero() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 5);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(-3);
            assertThat(gqs.getPowerBasedDamage(gd, perm)).isEqualTo(0);
        }

        @Test
        @DisplayName("returns 0 for a 0-power creature")
        void returnsZeroForZeroPower() {
            Permanent perm = addPermanent(player1Id, createCreature("Zero Power", 0, 1, CardColor.RED));

            assertThat(gqs.getPowerBasedDamage(gd, perm)).isEqualTo(0);
        }

        @Test
        @DisplayName("ignores Belligerent-Brontodon-style toughness-assign effect (combat-only)")
        void ignoresToughnessAssignEffect() {
            Permanent attacker = addPermanent(player1Id, createCreature("Wimpy Dinosaur", 1, 5, CardColor.GREEN));
            // Belligerent Brontodon-style: other own creatures assign combat damage equal to toughness
            addPermanent(player1Id, createCreatureWithStaticEffect("Belligerent Brontodon", 4, 4, CardColor.GREEN,
                    new com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect(GrantScope.OWN_CREATURES)));

            // Combat damage uses toughness (5), but non-combat power-based damage still uses power (1).
            assertThat(gqs.getEffectiveCombatDamage(gd, attacker)).isEqualTo(5);
            assertThat(gqs.getPowerBasedDamage(gd, attacker)).isEqualTo(1);
        }
    }

    // ===== hasProtectionFrom =====

    @Nested
    @DisplayName("hasProtectionFrom")
    class HasProtectionFrom {

        @Test
        @DisplayName("returns false for null source color")
        void returnsFalseForNullColor() {
            Permanent perm = addPermanent(player1Id, createMirranCrusader());

            assertThat(gqs.hasProtectionFrom(gd, perm, null)).isFalse();
        }

        @Test
        @DisplayName("returns false when no protection from that color")
        void returnsFalseWhenNoProtection() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.BLACK)).isFalse();
        }

        @Test
        @DisplayName("returns true from ProtectionFromColorsEffect")
        void returnsTrueWithProtectionEffect() {
            Permanent perm = addPermanent(player1Id, createMirranCrusader());

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.BLACK)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.GREEN)).isTrue();
        }

        @Test
        @DisplayName("returns false for non-protected color")
        void returnsFalseForNonProtectedColor() {
            Permanent perm = addPermanent(player1Id, createMirranCrusader());

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.RED)).isFalse();
            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.WHITE)).isFalse();
            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.BLUE)).isFalse();
        }

        @Test
        @DisplayName("returns true from chosen color")
        void returnsTrueFromChosenColor() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setChosenColor(CardColor.RED);

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.RED)).isTrue();
        }

        @Test
        @DisplayName("returns false for chosen color that doesn't match")
        void returnsFalseFromNonMatchingChosenColor() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setChosenColor(CardColor.RED);

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.BLUE)).isFalse();
        }

        @Test
        @DisplayName("returns true when protection color granted by static effect from another permanent")
        void returnsTrueForProtectionGrantedByStaticEffect() {
            addLordWithHandler(player1Id,
                    (ctx, eff, acc) -> acc.addProtectionColors(EnumSet.of(CardColor.RED)));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.RED)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.BLUE)).isFalse();
        }

        @Test
        @DisplayName("equipment with EQUIPPED_CREATURE scope does not give itself protection")
        void equipmentWithEquippedCreatureScopeDoesNotProtectItself() {
            Card swordCard = createArtifact("Sword of War and Peace");
            swordCard.addEffect(EffectSlot.STATIC,
                    new ProtectionFromColorsEffect(EnumSet.of(CardColor.RED, CardColor.WHITE), GrantScope.EQUIPPED_CREATURE));
            Permanent sword = addPermanent(player1Id, swordCard);

            assertThat(gqs.hasProtectionFrom(gd, sword, CardColor.RED)).isFalse();
            assertThat(gqs.hasProtectionFrom(gd, sword, CardColor.WHITE)).isFalse();
        }

        @Test
        @DisplayName("equipment with EQUIPPED_CREATURE scope grants protection to attached creature")
        void equipmentWithEquippedCreatureScopeProtectsAttachedCreature() {
            ProtectionFromColorsEffect protEffect =
                    new ProtectionFromColorsEffect(EnumSet.of(CardColor.RED, CardColor.WHITE), GrantScope.EQUIPPED_CREATURE);
            Card swordCard = createArtifact("Sword of War and Peace");
            swordCard.addEffect(EffectSlot.STATIC, protEffect);
            // Stub the static handler to mimic real static effect handler behavior
            when(staticEffectRegistry.getHandler(protEffect)).thenReturn((ctx, eff, acc) -> {
                var prot = (ProtectionFromColorsEffect) eff;
                if (ctx.source().isAttached()
                        && ctx.source().getAttachedTo().equals(ctx.target().getId())) {
                    acc.addProtectionColors(prot.colors());
                }
            });
            Permanent sword = addPermanent(player1Id, swordCard);

            Permanent creature = addPermanent(player1Id,
                    createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
            sword.setAttachedTo(creature.getId());

            assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
        }
    }

    // ===== hasProtectionFromSourceCardTypes =====

    @Nested
    @DisplayName("hasProtectionFromSourceCardTypes")
    class HasProtectionFromSourceCardTypes {

        @Test
        @DisplayName("returns false when no card type protection")
        void returnsFalseWhenNoProtection() {
            Permanent target = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Permanent source = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, source)).isFalse();
        }

        @Test
        @DisplayName("returns true when source has protected card type")
        void returnsTrueWhenSourceMatchesProtection() {
            Permanent target = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            target.getProtectionFromCardTypes().add(CardType.ARTIFACT);
            Permanent source = addPermanent(player2Id, createArtifact("Angel's Feather"));

            assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, source)).isTrue();
        }

        @Test
        @DisplayName("returns false when source does not match protected type")
        void returnsFalseWhenSourceDoesNotMatch() {
            Permanent target = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            target.getProtectionFromCardTypes().add(CardType.ENCHANTMENT);
            Permanent source = addPermanent(player2Id, createArtifact("Angel's Feather"));

            assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, source)).isFalse();
        }

        @Test
        @DisplayName("returns true against any source when the target has protection from everything")
        void returnsTrueFromProtectionFromEverything() {
            Card targetCard = createCreature("Progenitus", 10, 10, CardColor.GREEN);
            targetCard.addEffect(EffectSlot.STATIC, new ProtectionFromEverythingEffect());
            Permanent target = addPermanent(player1Id, targetCard);
            Permanent source = addPermanent(player2Id, createCreature("Red Goblin", 1, 1, CardColor.RED));

            assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, source)).isTrue();
        }
    }

    // ===== hasProtectionFromSource =====

    @Nested
    @DisplayName("hasProtectionFromSource")
    class HasProtectionFromSource {

        @Test
        @DisplayName("returns false when no protection")
        void returnsFalseWhenNoProtection() {
            Permanent target = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Permanent source = addPermanent(player2Id,
                    createCreature("Red Goblin", 1, 1, CardColor.RED));

            assertThat(gqs.hasProtectionFromSource(gd, target, source)).isFalse();
        }

        @Test
        @DisplayName("returns true when color protection matches")
        void returnsTrueFromColorProtection() {
            Permanent target = addPermanent(player1Id, createMirranCrusader());
            Permanent source = addPermanent(player2Id,
                    createCreature("Black Knight", 2, 2, CardColor.BLACK));

            assertThat(gqs.hasProtectionFromSource(gd, target, source)).isTrue();
        }

        @Test
        @DisplayName("returns true when card type protection matches")
        void returnsTrueFromCardTypeProtection() {
            Permanent target = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            target.getProtectionFromCardTypes().add(CardType.ARTIFACT);
            Permanent source = addPermanent(player2Id, createArtifact("Angel's Feather"));

            assertThat(gqs.hasProtectionFromSource(gd, target, source)).isTrue();
        }
    }

    // ===== cantBeTargetedBySpellColor =====

    @Nested
    @DisplayName("cantBeTargetedBySpellColor")
    class CantBeTargetedBySpellColor {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.BLUE)).isFalse();
        }

        @Test
        @DisplayName("returns false for null spell color")
        void returnsFalseForNullColor() {
            Permanent perm = addPermanent(player1Id, createCreatureWithStaticEffect("Karplusan Strider", 3, 4, CardColor.GREEN, TargetingRestrictionEffect.fromSpellColors(EnumSet.of(CardColor.BLUE, CardColor.BLACK))));

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, null)).isFalse();
        }

        @Test
        @DisplayName("returns true when matching color protection exists")
        void returnsTrueWithMatchingColor() {
            Permanent perm = addPermanent(player1Id, createCreatureWithStaticEffect("Karplusan Strider", 3, 4, CardColor.GREEN, TargetingRestrictionEffect.fromSpellColors(EnumSet.of(CardColor.BLUE, CardColor.BLACK))));

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.BLUE)).isTrue();
            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.BLACK)).isTrue();
        }

        @Test
        @DisplayName("returns false for non-matching color")
        void returnsFalseForNonMatchingColor() {
            Permanent perm = addPermanent(player1Id, createCreatureWithStaticEffect("Karplusan Strider", 3, 4, CardColor.GREEN, TargetingRestrictionEffect.fromSpellColors(EnumSet.of(CardColor.BLUE, CardColor.BLACK))));

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.RED)).isFalse();
        }

        @Test
        @DisplayName("returns true when granted by static effect from another permanent")
        void returnsTrueWhenGrantedByStaticEffect() {
            addLordWithHandler(player1Id,
                    (ctx, eff, acc) -> acc.addGrantedEffect(
                            TargetingRestrictionEffect.fromSpellColors(EnumSet.of(CardColor.BLUE))));
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.BLUE)).isTrue();
            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.RED)).isFalse();
        }
    }

    // ===== playerHasShroud =====

    @Nested
    @DisplayName("playerHasShroud")
    class PlayerHasShroud {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            assertThat(gqs.playerHasShroud(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("returns true when TrueBeliever is on battlefield")
        void returnsTrueWithTrueBeliever() {
            addPermanent(player1Id, createCreatureWithStaticEffect("True Believer", 2, 2, CardColor.WHITE, new GrantControllerShroudEffect()));

            assertThat(gqs.playerHasShroud(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("does not affect opponent")
        void doesNotAffectOpponent() {
            addPermanent(player1Id, createCreatureWithStaticEffect("True Believer", 2, 2, CardColor.WHITE, new GrantControllerShroudEffect()));

            assertThat(gqs.playerHasShroud(gd, player2Id)).isFalse();
        }
    }

    // ===== isUncounterable =====

    @Nested
    @DisplayName("isUncounterable")
    class IsUncounterable {

        @Test
        @DisplayName("returns false for non-creature")
        void returnsFalseForNonCreature() {
            Card sorcery = new Card();
            sorcery.setName("Lightning Bolt");
            sorcery.setType(CardType.INSTANT);

            assertThat(gqs.isUncounterable(gd, sorcery)).isFalse();
        }

        @Test
        @DisplayName("returns false for creature without effect on battlefield")
        void returnsFalseWithoutEffect() {
            Card creature = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.isUncounterable(gd, creature)).isFalse();
        }

        @Test
        @DisplayName("returns true when CreatureSpellsCantBeCounteredEffect on battlefield")
        void returnsTrueWithEffect() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Gaea's Herald", 1, 1, CardColor.GREEN, new CreatureSpellsCantBeCounteredEffect()));
            Card creature = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.isUncounterable(gd, creature)).isTrue();
        }

        @Test
        @DisplayName("works even if effect is on opponent's battlefield")
        void worksWithOpponentEffect() {
            addPermanent(player2Id, createCreatureWithStaticEffect("Gaea's Herald", 1, 1, CardColor.GREEN, new CreatureSpellsCantBeCounteredEffect()));
            Card creature = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.isUncounterable(gd, creature)).isTrue();
        }

        @Test
        @DisplayName("conditional 'can't be countered' applies while its condition holds (Banefire X>=5)")
        void conditionalCantBeCounteredWhenMet() {
            Card banefire = spellWithConditionalCantBeCountered();
            when(conditionEvaluationService.isMet(any(), any(), any())).thenReturn(true);

            assertThat(gqs.isUncounterable(gd, banefire)).isTrue();
        }

        @Test
        @DisplayName("conditional 'can't be countered' does not apply while its condition is unmet (Banefire X<5)")
        void conditionalCantBeCounteredWhenUnmet() {
            Card banefire = spellWithConditionalCantBeCountered();
            when(conditionEvaluationService.isMet(any(), any(), any())).thenReturn(false);

            assertThat(gqs.isUncounterable(gd, banefire)).isFalse();
        }

        private Card spellWithConditionalCantBeCountered() {
            Card banefire = new Card();
            banefire.setName("Banefire");
            banefire.setType(CardType.SORCERY);
            banefire.addEffect(EffectSlot.STATIC, new CantBeCounteredEffect(new SpellXAtLeast(5)));
            gd.stack.add(new StackEntry(StackEntryType.SORCERY_SPELL, banefire, player1Id,
                    "Banefire", new ArrayList<>()));
            return banefire;
        }

        @Test
        @DisplayName("controller's power-5-or-greater creature spell is protected (Spellbreaker Behemoth, boundary)")
        void controllerHighPowerCreatureSpellProtected() {
            addPermanent(player1Id, createCreatureWithStaticEffect(
                    "Spellbreaker Behemoth", 5, 5, CardColor.RED,
                    new ControllerCreatureSpellsCantBeCounteredEffect(5)));
            Card creature = creatureOnStack("Big Beast", 5, player1Id);

            assertThat(gqs.isUncounterable(gd, creature)).isTrue();
        }

        @Test
        @DisplayName("controller's below-threshold creature spell is not protected")
        void controllerLowPowerCreatureSpellNotProtected() {
            addPermanent(player1Id, createCreatureWithStaticEffect(
                    "Spellbreaker Behemoth", 5, 5, CardColor.RED,
                    new ControllerCreatureSpellsCantBeCounteredEffect(5)));
            Card creature = creatureOnStack("Small Beast", 4, player1Id);

            assertThat(gqs.isUncounterable(gd, creature)).isFalse();
        }

        @Test
        @DisplayName("high-power creature spell is not protected by an opponent's Behemoth")
        void opponentBehemothDoesNotProtect() {
            addPermanent(player2Id, createCreatureWithStaticEffect(
                    "Spellbreaker Behemoth", 5, 5, CardColor.RED,
                    new ControllerCreatureSpellsCantBeCounteredEffect(5)));
            Card creature = creatureOnStack("Big Beast", 8, player1Id);

            assertThat(gqs.isUncounterable(gd, creature)).isFalse();
        }

        private Card creatureOnStack(String name, int power, UUID controllerId) {
            Card creature = createCreature(name, power, power, CardColor.GREEN);
            gd.stack.add(new StackEntry(StackEntryType.CREATURE_SPELL, creature, controllerId,
                    name, new ArrayList<>()));
            return creature;
        }
    }

    // ===== hasAnimateArtifactEffect =====

    @Nested
    @DisplayName("hasAnimateArtifactEffect")
    class HasAnimateArtifactEffect {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            assertThat(gqs.hasAnimateArtifactEffect(gd)).isFalse();
        }

        @Test
        @DisplayName("returns true with March of the Machines on battlefield")
        void returnsTrueWithMarchOfTheMachines() {
            addPermanent(player1Id, createEnchantmentWithStaticEffect("March of the Machines", new AnimateNoncreatureArtifactsEffect()));

            assertThat(gqs.hasAnimateArtifactEffect(gd)).isTrue();
        }
    }

    // ===== getDamageMultiplier / applyDamageMultiplier =====

    @Nested
    @DisplayName("getDamageMultiplier and applyDamageMultiplier")
    class DamageMultiplier {

        @Test
        @DisplayName("returns 1 by default")
        void returnsOneByDefault() {
            assertThat(gqs.getDamageMultiplier(gd)).isEqualTo(1);
        }

        @Test
        @DisplayName("returns 2 with one Furnace of Rath")
        void returnsTwoWithOneFurnace() {
            addPermanent(player1Id, createEnchantmentWithStaticEffect("Furnace of Rath", new DoubleDamageEffect()));

            assertThat(gqs.getDamageMultiplier(gd)).isEqualTo(2);
        }

        @Test
        @DisplayName("returns 4 with two Furnaces of Rath")
        void returnsFourWithTwoFurnaces() {
            addPermanent(player1Id, createEnchantmentWithStaticEffect("Furnace of Rath", new DoubleDamageEffect()));
            addPermanent(player2Id, createEnchantmentWithStaticEffect("Furnace of Rath", new DoubleDamageEffect()));

            assertThat(gqs.getDamageMultiplier(gd)).isEqualTo(4);
        }

        @Test
        @DisplayName("applyDamageMultiplier applies correctly")
        void applyMultiplier() {
            addPermanent(player1Id, createEnchantmentWithStaticEffect("Furnace of Rath", new DoubleDamageEffect()));

            assertThat(gqs.applyDamageMultiplier(gd, 3)).isEqualTo(6);
        }

        @Test
        @DisplayName("applyDamageMultiplier with no effect returns same value")
        void applyMultiplierNoEffect() {
            assertThat(gqs.applyDamageMultiplier(gd, 3)).isEqualTo(3);
        }
    }

    // ===== getEnchantedPlayerDamageMultiplier =====

    @Nested
    @DisplayName("getEnchantedPlayerDamageMultiplier")
    class EnchantedPlayerDamageMultiplier {

        @Test
        @DisplayName("returns 1 by default")
        void returnsOneByDefault() {
            assertThat(gqs.getEnchantedPlayerDamageMultiplier(gd, player2Id)).isEqualTo(1);
        }

        @Test
        @DisplayName("returns 2 with one Curse of Bloodletting enchanting the player")
        void returnsTwoWithOneCurse() {
            addCurseEnchanting(player1Id, player2Id);

            assertThat(gqs.getEnchantedPlayerDamageMultiplier(gd, player2Id)).isEqualTo(2);
        }

        @Test
        @DisplayName("returns 4 with two Curses enchanting the player")
        void returnsFourWithTwoCurses() {
            addCurseEnchanting(player1Id, player2Id);
            addCurseEnchanting(player1Id, player2Id);

            assertThat(gqs.getEnchantedPlayerDamageMultiplier(gd, player2Id)).isEqualTo(4);
        }

        @Test
        @DisplayName("does not apply to a player the curse does not enchant")
        void doesNotApplyToOtherPlayer() {
            addCurseEnchanting(player1Id, player2Id);

            assertThat(gqs.getEnchantedPlayerDamageMultiplier(gd, player1Id)).isEqualTo(1);
        }

        private void addCurseEnchanting(UUID controllerId, UUID enchantedPlayerId) {
            Permanent curse = addPermanent(controllerId,
                    createEnchantmentWithStaticEffect("Curse of Bloodletting", new DoubleDamageToEnchantedPlayerEffect()));
            curse.setAttachedTo(enchantedPlayerId);
        }
    }

    // ===== getControllerDamageMultiplier =====

    @Nested
    @DisplayName("getControllerDamageMultiplier")
    class ControllerDamageMultiplier {

        @Test
        @DisplayName("returns 1 by default for combat damage")
        void returnsOneByDefaultCombat() {
            assertThat(gqs.getControllerDamageMultiplier(gd, player1Id, null, true)).isEqualTo(1);
        }

        @Test
        @DisplayName("returns 1 for null controllerId")
        void returnsOneForNull() {
            assertThat(gqs.getControllerDamageMultiplier(gd, null, null, true)).isEqualTo(1);
        }

        @Test
        @DisplayName("returns 2 for combat when appliesToCombatDamage is true")
        void returnsTwoForCombatWithAllSourcesEffect() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Angrath's Marauders", 4, 4, CardColor.RED,
                    new DoubleControllerDamageEffect(null, true)));

            assertThat(gqs.getControllerDamageMultiplier(gd, player1Id, null, true)).isEqualTo(2);
        }

        @Test
        @DisplayName("returns 1 for combat when appliesToCombatDamage is false")
        void returnsOneForCombatWithSpellOnlyEffect() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Fire Servant", 4, 3, CardColor.RED,
                    new DoubleControllerDamageEffect(
                            new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                            false)));

            assertThat(gqs.getControllerDamageMultiplier(gd, player1Id, null, true)).isEqualTo(1);
        }

        @Test
        @DisplayName("returns 2 for matching stack entry when filter matches")
        void returnsTwoForMatchingStackEntry() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Fire Servant", 4, 3, CardColor.RED,
                    new DoubleControllerDamageEffect(
                            new StackEntryAllOfPredicate(List.of(
                                    new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                    new StackEntryColorInPredicate(Set.of(CardColor.RED))
                            )),
                            false)));

            Card redSpell = new Card();
            redSpell.setName("Shock");
            redSpell.setColors(List.of(CardColor.RED));
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, redSpell, player1Id,
                    "Shock", new ArrayList<>(), null);

            assertThat(gqs.getControllerDamageMultiplier(gd, player1Id, entry, false)).isEqualTo(2);
        }

        @Test
        @DisplayName("returns 1 for non-matching stack entry color")
        void returnsOneForNonMatchingColor() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Fire Servant", 4, 3, CardColor.RED,
                    new DoubleControllerDamageEffect(
                            new StackEntryColorInPredicate(Set.of(CardColor.RED)),
                            false)));

            Card greenSpell = new Card();
            greenSpell.setName("Giant Growth");
            greenSpell.setColors(List.of(CardColor.GREEN));
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, greenSpell, player1Id,
                    "Giant Growth", new ArrayList<>(), null);

            assertThat(gqs.getControllerDamageMultiplier(gd, player1Id, entry, false)).isEqualTo(1);
        }

        @Test
        @DisplayName("does not affect opponent")
        void doesNotAffectOpponent() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Angrath's Marauders", 4, 4, CardColor.RED,
                    new DoubleControllerDamageEffect(null, true)));

            assertThat(gqs.getControllerDamageMultiplier(gd, player2Id, null, true)).isEqualTo(1);
        }

        @Test
        @DisplayName("returns 4 with two DoubleControllerDamageEffect permanents")
        void returnsFourWithTwoEffects() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Angrath's Marauders", 4, 4, CardColor.RED,
                    new DoubleControllerDamageEffect(null, true)));
            addPermanent(player1Id, createCreatureWithStaticEffect("Angrath's Marauders", 4, 4, CardColor.RED,
                    new DoubleControllerDamageEffect(null, true)));

            assertThat(gqs.getControllerDamageMultiplier(gd, player1Id, null, true)).isEqualTo(4);
        }

        @Test
        @DisplayName("null filter matches any stack entry")
        void nullFilterMatchesAnyStackEntry() {
            addPermanent(player1Id, createCreatureWithStaticEffect("Angrath's Marauders", 4, 4, CardColor.RED,
                    new DoubleControllerDamageEffect(null, true)));

            Card greenSpell = new Card();
            greenSpell.setName("Giant Growth");
            greenSpell.setColors(List.of(CardColor.GREEN));
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, greenSpell, player1Id,
                    "Giant Growth", new ArrayList<>(), null);

            assertThat(gqs.getControllerDamageMultiplier(gd, player1Id, entry, false)).isEqualTo(2);
        }
    }

    // ===== getColorSourceDamageBonus =====

    @Nested
    @DisplayName("getColorSourceDamageBonus")
    class ColorSourceDamageBonus {

        private void setBonus(UUID playerId, CardColor color, int bonus) {
            gd.colorSourceDamageBonusThisTurn
                    .computeIfAbsent(playerId, k -> new java.util.concurrent.ConcurrentHashMap<>())
                    .put(color, bonus);
        }

        @Test
        @DisplayName("returns 0 when no bonus set")
        void returnsZeroByDefault() {
            assertThat(gqs.getColorSourceDamageBonus(gd, player1Id, List.of(CardColor.RED))).isZero();
        }

        @Test
        @DisplayName("returns bonus for matching color source")
        void returnsBonusForMatchingColor() {
            setBonus(player1Id, CardColor.RED, 2);

            assertThat(gqs.getColorSourceDamageBonus(gd, player1Id, List.of(CardColor.RED))).isEqualTo(2);
        }

        @Test
        @DisplayName("returns 0 for non-matching color source")
        void returnsZeroForNonMatchingColor() {
            setBonus(player1Id, CardColor.RED, 2);

            assertThat(gqs.getColorSourceDamageBonus(gd, player1Id, List.of(CardColor.GREEN))).isZero();
        }

        @Test
        @DisplayName("returns bonus for multicolored source including matching color")
        void returnsBonusForMulticolorWithMatch() {
            setBonus(player1Id, CardColor.RED, 2);

            assertThat(gqs.getColorSourceDamageBonus(gd, player1Id, List.of(CardColor.RED, CardColor.GREEN))).isEqualTo(2);
        }

        @Test
        @DisplayName("returns 0 for different controller")
        void returnsZeroForDifferentController() {
            setBonus(player1Id, CardColor.RED, 2);

            assertThat(gqs.getColorSourceDamageBonus(gd, player2Id, List.of(CardColor.RED))).isZero();
        }

        @Test
        @DisplayName("returns 0 for null colors")
        void returnsZeroForNullColors() {
            setBonus(player1Id, CardColor.RED, 2);

            assertThat(gqs.getColorSourceDamageBonus(gd, player1Id, null)).isZero();
        }

        @Test
        @DisplayName("applyDamageMultiplier includes color bonus for matching spell")
        void applyDamageMultiplierIncludesBonus() {
            setBonus(player1Id, CardColor.RED, 2);

            Card redSpell = new Card();
            redSpell.setName("Red Spell");
            redSpell.setColors(List.of(CardColor.RED));
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, redSpell, player1Id,
                    "Red Spell", new ArrayList<>(), null);

            // 3 base + 2 bonus = 5
            assertThat(gqs.applyDamageMultiplier(gd, 3, entry)).isEqualTo(5);
        }

        @Test
        @DisplayName("applyDamageMultiplier does not add bonus for non-matching spell")
        void applyDamageMultiplierNoBonusForNonMatching() {
            setBonus(player1Id, CardColor.RED, 2);

            Card greenSpell = new Card();
            greenSpell.setName("Green Spell");
            greenSpell.setColors(List.of(CardColor.GREEN));
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, greenSpell, player1Id,
                    "Green Spell", new ArrayList<>(), null);

            assertThat(gqs.applyDamageMultiplier(gd, 3, entry)).isEqualTo(3);
        }

        @Test
        @DisplayName("applyCombatDamageMultiplier includes color bonus for matching creature")
        void applyCombatDamageMultiplierIncludesBonus() {
            setBonus(player1Id, CardColor.RED, 2);

            Card redCreature = createCreature("Red Goblin", 2, 1, CardColor.RED);
            redCreature.setColors(List.of(CardColor.RED));
            Permanent perm = addPermanent(player1Id, redCreature);

            // 2 base + 2 bonus = 4
            assertThat(gqs.applyCombatDamageMultiplier(gd, 2, perm, null)).isEqualTo(4);
        }

        @Test
        @DisplayName("applyCombatDamageMultiplier does not add bonus for zero damage")
        void applyCombatDamageMultiplierNoBonusForZeroDamage() {
            setBonus(player1Id, CardColor.RED, 2);

            Card redCreature = createCreature("Red Goblin", 2, 1, CardColor.RED);
            redCreature.setColors(List.of(CardColor.RED));
            Permanent perm = addPermanent(player1Id, redCreature);

            // 0 damage -> no bonus applied
            assertThat(gqs.applyCombatDamageMultiplier(gd, 0, perm, null)).isZero();
        }
    }

    // ===== isPreventedFromDealingDamage =====

    @Nested
    @DisplayName("isPreventedFromDealingDamage")
    class IsPreventedFromDealingDamage {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.isPreventedFromDealingDamage(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true when permanent is in prevention set")
        void returnsTrueWhenInPreventionSet() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            gd.permanentsPreventedFromDealingDamage.add(perm.getId());

            assertThat(gqs.isPreventedFromDealingDamage(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true when color damage prevention is active")
        void returnsTrueWithColorPrevention() {
            Card card = createCreature("Green Bear", 2, 2, CardColor.GREEN);
            Permanent perm = addPermanent(player1Id, card);
            gd.preventDamageFromColors.add(CardColor.GREEN);

            assertThat(gqs.isPreventedFromDealingDamage(gd, perm)).isTrue();
        }
    }

    // ===== isDamageFromSourcePrevented =====

    @Nested
    @DisplayName("isDamageFromSourcePrevented")
    class IsDamageFromSourcePrevented {

        @Test
        @DisplayName("returns false when no prevention")
        void returnsFalseByDefault() {
            assertThat(gqs.isDamageFromSourcePrevented(gd, CardColor.RED)).isFalse();
        }

        @Test
        @DisplayName("returns true when color is in prevention set")
        void returnsTrueWhenColorPrevented() {
            gd.preventDamageFromColors.add(CardColor.RED);

            assertThat(gqs.isDamageFromSourcePrevented(gd, CardColor.RED)).isTrue();
        }

        @Test
        @DisplayName("returns false when source color is null")
        void returnsFalseForNullColor() {
            gd.preventDamageFromColors.add(CardColor.RED);

            assertThat(gqs.isDamageFromSourcePrevented(gd, null)).isFalse();
        }

        @Test
        @DisplayName("returns false when different color is prevented")
        void returnsFalseForDifferentColor() {
            gd.preventDamageFromColors.add(CardColor.RED);

            assertThat(gqs.isDamageFromSourcePrevented(gd, CardColor.BLUE)).isFalse();
        }
    }

    // ===== isEnchanted =====

    @Nested
    @DisplayName("isEnchanted")
    class IsEnchanted {

        @Test
        @DisplayName("returns false when no aura attached")
        void returnsFalseWhenNoAura() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.isEnchanted(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true when aura is attached")
        void returnsTrueWithAura() {
            Permanent creature = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Permanent aura = addPermanent(player1Id, createAura("Heart of Light", new PreventAllDamageToAndByEnchantedCreatureEffect()));
            aura.setAttachedTo(creature.getId());

            assertThat(gqs.isEnchanted(gd, creature)).isTrue();
        }

        @Test
        @DisplayName("returns false when non-aura attachment (equipment)")
        void returnsFalseWithEquipment() {
            Permanent creature = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Permanent equipment = addPermanent(player1Id, createArtifact("Some Equipment"));
            equipment.setAttachedTo(creature.getId());

            assertThat(gqs.isEnchanted(gd, creature)).isFalse();
        }
    }

    // ===== hasAuraWithEffect =====

    @Nested
    @DisplayName("hasAuraWithEffect")
    class HasAuraWithEffect {

        @Test
        @DisplayName("returns true when aura with matching effect is attached")
        void returnsTrueWithMatchingEffect() {
            Permanent creature = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Permanent aura = addPermanent(player1Id, createAura("Heart of Light", new PreventAllDamageToAndByEnchantedCreatureEffect()));
            aura.setAttachedTo(creature.getId());

            assertThat(gqs.hasAuraWithEffect(gd, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)).isTrue();
        }

        @Test
        @DisplayName("returns false when no attachment")
        void returnsFalseWhenNoAttachment() {
            Permanent creature = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.hasAuraWithEffect(gd, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)).isFalse();
        }

        @Test
        @DisplayName("returns false when attached permanent has different effect")
        void returnsFalseWithDifferentEffect() {
            Permanent creature = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Permanent aura = addPermanent(player1Id, createAura("Heart of Light", new PreventAllDamageToAndByEnchantedCreatureEffect()));
            aura.setAttachedTo(creature.getId());

            assertThat(gqs.hasAuraWithEffect(gd, creature, CantBeBlockedEffect.class)).isFalse();
        }

        @Test
        @DisplayName("unwraps EnchantedPermanentConditionalEffect when predicate matches")
        void unwrapsEnchantedPermanentConditionalWhenPredicateMatches() {
            Permanent creature = addPermanent(player1Id, createCreatureWithSubtypes("Honor Guard", 1, 1, CardColor.WHITE, List.of(CardSubtype.HUMAN)));
            Permanent aura = addPermanent(player1Id, createAura("Bonds of Faith",
                    new EnchantedPermanentConditionalEffect(
                            new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                            new PreventAllDamageToAndByEnchantedCreatureEffect(),
                            new EnchantedCreatureCantAttackOrBlockEffect())));
            aura.setAttachedTo(creature.getId());

            assertThat(gqs.hasAuraWithEffect(gd, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)).isTrue();
            assertThat(gqs.hasAuraWithEffect(gd, creature, EnchantedCreatureCantAttackOrBlockEffect.class)).isFalse();
        }

        @Test
        @DisplayName("unwraps EnchantedPermanentConditionalEffect when predicate does not match")
        void unwrapsEnchantedPermanentConditionalWhenPredicateDoesNotMatch() {
            Permanent creature = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            Permanent aura = addPermanent(player1Id, createAura("Bonds of Faith",
                    new EnchantedPermanentConditionalEffect(
                            new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                            new PreventAllDamageToAndByEnchantedCreatureEffect(),
                            new EnchantedCreatureCantAttackOrBlockEffect())));
            aura.setAttachedTo(creature.getId());

            assertThat(gqs.hasAuraWithEffect(gd, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)).isFalse();
            assertThat(gqs.hasAuraWithEffect(gd, creature, EnchantedCreatureCantAttackOrBlockEffect.class)).isTrue();
        }
    }

    // ===== sourceHasKeyword =====

    @Nested
    @DisplayName("sourceHasKeyword")
    class SourceHasKeyword {

        @Test
        @DisplayName("uses explicit source when provided")
        void usesExplicitSource() {
            Permanent explicitSource = addPermanent(player1Id, createMirranCrusader());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)), player1Id,
                    "Test", List.of());

            assertThat(gqs.sourceHasKeyword(gd, entry, explicitSource, Keyword.DOUBLE_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("falls back to entry source when no explicit source")
        void fallsBackToEntrySource() {
            Permanent source = addPermanent(player1Id, createMirranCrusader());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, createMirranCrusader(), player1Id,
                    "Test", List.of(), null, source.getId());

            assertThat(gqs.sourceHasKeyword(gd, entry, null, Keyword.DOUBLE_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("returns false when entry source permanent lacks keyword")
        void returnsFalseWhenEntrySourceLacksKeyword() {
            Permanent source = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)), player1Id,
                    "Test", List.of(), null, source.getId());

            assertThat(gqs.sourceHasKeyword(gd, entry, null, Keyword.FLYING)).isFalse();
        }

        @Test
        @DisplayName("returns false when no source permanent")
        void returnsFalseWhenNoSource() {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)), player1Id,
                    "Test", List.of());

            assertThat(gqs.sourceHasKeyword(gd, entry, null, Keyword.FLYING)).isFalse();
        }
    }

    // ===== countControlledSubtypePermanents =====

    @Nested
    @DisplayName("countControlledSubtypePermanents")
    class CountControlledSubtypePermanents {

        @Test
        @DisplayName("returns 0 for empty battlefield")
        void returnsZeroForEmptyBattlefield() {
            assertThat(gqs.countControlledSubtypePermanents(gd, player1Id, CardSubtype.ELF)).isEqualTo(0);
        }

        @Test
        @DisplayName("counts permanents with matching subtype")
        void countsMatchingSubtype() {
            Card elf1 = createCreature("Elf 1", 1, 1, CardColor.GREEN);
            elf1.setSubtypes(List.of(CardSubtype.ELF));
            Card elf2 = createCreature("Elf 2", 2, 2, CardColor.GREEN);
            elf2.setSubtypes(List.of(CardSubtype.ELF));
            Card nonElf = createCreature("Bear", 2, 2, CardColor.GREEN);
            nonElf.setSubtypes(List.of(CardSubtype.BEAR));

            addPermanent(player1Id, elf1);
            addPermanent(player1Id, elf2);
            addPermanent(player1Id, nonElf);

            assertThat(gqs.countControlledSubtypePermanents(gd, player1Id, CardSubtype.ELF)).isEqualTo(2);
        }

        @Test
        @DisplayName("does not count opponent's permanents")
        void doesNotCountOpponent() {
            Card elf = createCreature("Elf", 1, 1, CardColor.GREEN);
            elf.setSubtypes(List.of(CardSubtype.ELF));
            addPermanent(player2Id, elf);

            assertThat(gqs.countControlledSubtypePermanents(gd, player1Id, CardSubtype.ELF)).isEqualTo(0);
        }
    }

    /**
     * Tests for {@link GameQueryService#permanentWouldHaveSubtype} — the CR 614.12
     * replacement-effect lookahead that determines what subtypes a permanent would have
     * on the battlefield before it actually enters.
     *
     * Uses a real {@link StaticEffectHandlerRegistry} with manually registered handlers
     * (not the mocked one from the outer class).
     */
    @Nested
    @DisplayName("permanentWouldHaveSubtype — CR 614.12 lookahead")
    class PermanentWouldHaveSubtype {

        private StaticEffectHandlerRegistry realRegistry;
        private GameQueryService lookaheadGqs;
        private GameData lookaheadGameData;
        private UUID playerId;
        private UUID opponentId;

        @BeforeEach
        void setUp() {
            realRegistry = new StaticEffectHandlerRegistry();
            realRegistry.register(GrantSubtypeEffect.class, (context, effect, accumulator) -> {
                var grant = (GrantSubtypeEffect) effect;
                boolean matches = switch (grant.scope()) {
                    case OWN_CREATURES, OWN_PERMANENTS -> context.targetOnSameBattlefield();
                    case ALL_CREATURES, ALL_PERMANENTS -> true;
                    case OPPONENT_CREATURES -> !context.targetOnSameBattlefield();
                    default -> false;
                };
                if (matches) {
                    accumulator.addGrantedSubtype(grant.subtype());
                }
            });
            realRegistry.register(GrantKeywordEffect.class, (context, effect, accumulator) -> {
                var grant = (GrantKeywordEffect) effect;
                boolean matches = switch (grant.scope()) {
                    case OWN_CREATURES, OWN_PERMANENTS -> context.targetOnSameBattlefield();
                    case ALL_CREATURES, ALL_PERMANENTS -> true;
                    default -> false;
                };
                if (matches) {
                    accumulator.addKeywords(grant.keywords());
                }
            });

            lookaheadGqs = new GameQueryService(realRegistry);
            PredicateEvaluationService lookaheadEvaluator = new PredicateEvaluationService(lookaheadGqs);
            ReflectionTestUtils.setField(lookaheadGqs, "predicateEvaluationService", lookaheadEvaluator);
            LayerSystemService lookaheadLayerSystem = new LayerSystemService();
            ReflectionTestUtils.setField(lookaheadLayerSystem, "predicateEvaluationService", lookaheadEvaluator);
            ReflectionTestUtils.setField(lookaheadLayerSystem, "staticEffectRegistry", realRegistry);
            ReflectionTestUtils.setField(lookaheadLayerSystem, "gameQueryService", lookaheadGqs);
            ReflectionTestUtils.setField(lookaheadGqs, "layerSystemService", lookaheadLayerSystem);

            playerId = UUID.randomUUID();
            opponentId = UUID.randomUUID();
            lookaheadGameData = new GameData(UUID.randomUUID(), "test", playerId, "TestPlayer");
            lookaheadGameData.orderedPlayerIds.add(playerId);
            lookaheadGameData.orderedPlayerIds.add(opponentId);
            lookaheadGameData.playerBattlefields.put(playerId, Collections.synchronizedList(new ArrayList<>()));
            lookaheadGameData.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));
        }

        private Card createCreatureCard(String name, List<CardSubtype> subtypes) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.CREATURE);
            card.setSubtypes(subtypes);
            return card;
        }

        private Card createCreatureCardWithKeywords(String name, List<CardSubtype> subtypes, Keyword... keywords) {
            Card card = createCreatureCard(name, subtypes);
            card.setKeywords(java.util.EnumSet.copyOf(java.util.Set.of(keywords)));
            return card;
        }

        @Test
        @DisplayName("Returns true when permanent has the subtype naturally")
        void naturalSubtype() {
            Permanent entering = new Permanent(createCreatureCard("Elite Vanguard", List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER)));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
        }

        @Test
        @DisplayName("Returns false when permanent does not have the subtype and no effects grant it")
        void noMatchingSubtype() {
            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isFalse();
        }

        @Test
        @DisplayName("Returns true when permanent has Changeling keyword (all creature subtypes)")
        void changelingHasAllCreatureSubtypes() {
            Permanent entering = new Permanent(
                    createCreatureCardWithKeywords("Changeling Outcast", List.of(CardSubtype.SHAPESHIFTER), Keyword.CHANGELING));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.GOBLIN)).isTrue();
        }

        @Test
        @DisplayName("Changeling does not match non-creature subtypes (e.g., Equipment, Aura)")
        void changelingDoesNotMatchNonCreatureSubtypes() {
            Permanent entering = new Permanent(
                    createCreatureCardWithKeywords("Changeling Outcast", List.of(CardSubtype.SHAPESHIFTER), Keyword.CHANGELING));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.EQUIPMENT)).isFalse();
        }

        @Test
        @DisplayName("Returns true when a battlefield permanent grants the subtype to own creatures")
        void subtypeGrantedByBattlefieldPermanent() {
            Card xenograftCard = new Card();
            xenograftCard.setName("Xenograft");
            xenograftCard.setType(CardType.ENCHANTMENT);
            xenograftCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            Permanent xenograft = new Permanent(xenograftCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(xenograft);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
        }

        @Test
        @DisplayName("Opponent's subtype-granting effect with OWN_CREATURES scope does not affect your creatures")
        void opponentOwnCreaturesScopeDoesNotAffect() {
            Card xenograftCard = new Card();
            xenograftCard.setName("Xenograft");
            xenograftCard.setType(CardType.ENCHANTMENT);
            xenograftCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            Permanent xenograft = new Permanent(xenograftCard);
            lookaheadGameData.playerBattlefields.get(opponentId).add(xenograft);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isFalse();
        }

        @Test
        @DisplayName("ALL_CREATURES scope grants subtype regardless of controller")
        void allCreaturesScopeAffectsEveryone() {
            Card conspiracyCard = new Card();
            conspiracyCard.setName("Conspiracy");
            conspiracyCard.setType(CardType.ENCHANTMENT);
            conspiracyCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.ALL_CREATURES));
            Permanent conspiracy = new Permanent(conspiracyCard);
            lookaheadGameData.playerBattlefields.get(opponentId).add(conspiracy);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
        }

        @Test
        @DisplayName("Returns true when a battlefield permanent grants Changeling to own creatures")
        void changelingGrantedByStaticEffect() {
            Card maskCard = new Card();
            maskCard.setName("Maskwood Nexus");
            maskCard.setType(CardType.ARTIFACT);
            maskCard.addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.CHANGELING, GrantScope.OWN_CREATURES));
            Permanent mask = new Permanent(maskCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(mask);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isTrue();
            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.GOBLIN)).isTrue();
        }

        @Test
        @DisplayName("Simultaneously-entered permanent with subtype grant is excluded from lookahead")
        void simultaneouslyEnteredExcludedFromLookahead() {
            Card xenograftCard = new Card();
            xenograftCard.setName("Xenograft");
            xenograftCard.setType(CardType.ENCHANTMENT);
            xenograftCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            Permanent xenograft = new Permanent(xenograftCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(xenograft);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(xenograft), CardSubtype.HUMAN)).isFalse();
        }

        @Test
        @DisplayName("Pre-existing battlefield permanent IS visible even when other simultaneous entries are excluded")
        void preExistingPermanentVisibleDespiteSimultaneousExclusion() {
            Card xenograftCard = new Card();
            xenograftCard.setName("Xenograft");
            xenograftCard.setType(CardType.ENCHANTMENT);
            xenograftCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            Permanent preExistingXenograft = new Permanent(xenograftCard);
            lookaheadGameData.playerBattlefields.get(playerId).add(preExistingXenograft);

            Permanent simultaneousCreature = new Permanent(createCreatureCard("Elvish Mystic", List.of(CardSubtype.ELF)));
            lookaheadGameData.playerBattlefields.get(playerId).add(simultaneousCreature);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(simultaneousCreature), CardSubtype.HUMAN)).isTrue();
        }

        @Test
        @DisplayName("Empty simultaneous list means all battlefield permanents are visible")
        void emptySimultaneousListMeansAllVisible() {
            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            assertThat(lookaheadGqs.permanentWouldHaveSubtype(
                    lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN)).isFalse();
        }

        @Test
        @DisplayName("Lookahead restores battlefield exactly: entering removed, excluded re-added")
        void lookaheadCleansUpTemporaryPermanents() {
            Permanent simEntry = new Permanent(createCreatureCard("Another Bear", List.of(CardSubtype.BEAR)));
            lookaheadGameData.playerBattlefields.get(playerId).add(simEntry);

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));

            List<Permanent> bfBefore = new ArrayList<>(lookaheadGameData.playerBattlefields.get(playerId));

            lookaheadGqs.permanentWouldHaveSubtype(lookaheadGameData, entering, playerId, List.of(simEntry), CardSubtype.HUMAN);

            assertThat(lookaheadGameData.playerBattlefields.get(playerId)).containsExactlyElementsOf(bfBefore);
        }

        @Test
        @DisplayName("Lookahead restores battlefield even when static bonus computation fails")
        void lookaheadCleansUpOnException() {
            realRegistry.register(GrantSubtypeEffect.class, (context, effect, accumulator) -> {
                throw new RuntimeException("simulated failure");
            });

            Card badCard = new Card();
            badCard.setName("Bad Enchantment");
            badCard.setType(CardType.ENCHANTMENT);
            badCard.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.HUMAN, GrantScope.OWN_CREATURES));
            lookaheadGameData.playerBattlefields.get(playerId).add(new Permanent(badCard));

            Permanent entering = new Permanent(createCreatureCard("Grizzly Bears", List.of(CardSubtype.BEAR)));
            List<Permanent> bfBefore = new ArrayList<>(lookaheadGameData.playerBattlefields.get(playerId));

            try {
                lookaheadGqs.permanentWouldHaveSubtype(lookaheadGameData, entering, playerId, List.of(), CardSubtype.HUMAN);
            } catch (RuntimeException ignored) {
                // expected
            }

            assertThat(lookaheadGameData.playerBattlefields.get(playerId)).containsExactlyElementsOf(bfBefore);
        }
    }

    @Nested
    @DisplayName("canActivateManaAbility")
    class CanActivateManaAbility {

        @Test
        @DisplayName("returns true when no locks are on the battlefield")
        void returnsTrue_whenNoLocks() {
            Card artifactCard = createArtifact("Leaden Myr");
            artifactCard.setAdditionalTypes(EnumSet.of(CardType.CREATURE));
            Permanent myr = addPermanent(player1Id, artifactCard);

            assertThat(gqs.canActivateManaAbility(gd, myr)).isTrue();
        }

        @Test
        @DisplayName("returns false when Stony Silence locks artifact mana abilities")
        void returnsFalse_whenStonySilenceLocks() {
            // Stony Silence on opponent's battlefield
            Card stonySilence = createEnchantmentWithStaticEffect("Stony Silence",
                    new ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(new PermanentIsArtifactPredicate()));
            addPermanent(player2Id, stonySilence);

            // Artifact creature on player1's battlefield
            Card myrCard = createArtifactCreature("Leaden Myr", 1, 1, List.of(CardSubtype.MYR));
            Permanent myr = addPermanent(player1Id, myrCard);

            assertThat(gqs.canActivateManaAbility(gd, myr)).isFalse();
        }

        @Test
        @DisplayName("returns true for non-artifact when Stony Silence is present")
        void returnsTrue_forNonArtifactUnderStonySilence() {
            Card stonySilence = createEnchantmentWithStaticEffect("Stony Silence",
                    new ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(new PermanentIsArtifactPredicate()));
            addPermanent(player2Id, stonySilence);

            Card landCard = createLand("Forest");
            Permanent forest = addPermanent(player1Id, landCard);

            assertThat(gqs.canActivateManaAbility(gd, forest)).isTrue();
        }

        @Test
        @DisplayName("returns false when chosen-name lock blocks mana abilities")
        void returnsFalse_whenChosenNameLockBlocksMana() {
            // Phyrexian Revoker names "Leaden Myr" and blocks mana abilities
            Card revoker = createCreatureWithStaticEffect("Phyrexian Revoker", 2, 1, null,
                    new ActivatedAbilitiesOfChosenNameCantBeActivatedEffect(true));
            Permanent revokerPerm = addPermanent(player2Id, revoker);
            revokerPerm.setChosenName("Leaden Myr");

            Card myrCard = createArtifactCreature("Leaden Myr", 1, 1, List.of(CardSubtype.MYR));
            Permanent myr = addPermanent(player1Id, myrCard);

            assertThat(gqs.canActivateManaAbility(gd, myr)).isFalse();
        }

        @Test
        @DisplayName("returns true when chosen-name lock does not block mana abilities")
        void returnsTrue_whenChosenNameLockDoesNotBlockMana() {
            // Pithing Needle names "Leaden Myr" but does NOT block mana abilities
            Card needle = createEnchantmentWithStaticEffect("Pithing Needle",
                    new ActivatedAbilitiesOfChosenNameCantBeActivatedEffect(false));
            Permanent needlePerm = addPermanent(player2Id, needle);
            needlePerm.setChosenName("Leaden Myr");

            Card myrCard = createArtifactCreature("Leaden Myr", 1, 1, List.of(CardSubtype.MYR));
            Permanent myr = addPermanent(player1Id, myrCard);

            assertThat(gqs.canActivateManaAbility(gd, myr)).isTrue();
        }

        @Test
        @DisplayName("returns true when chosen-name lock names a different card")
        void returnsTrue_whenChosenNameDoesNotMatch() {
            Card revoker = createCreatureWithStaticEffect("Phyrexian Revoker", 2, 1, null,
                    new ActivatedAbilitiesOfChosenNameCantBeActivatedEffect(true));
            Permanent revokerPerm = addPermanent(player2Id, revoker);
            revokerPerm.setChosenName("Sol Ring");

            Card myrCard = createArtifactCreature("Leaden Myr", 1, 1, List.of(CardSubtype.MYR));
            Permanent myr = addPermanent(player1Id, myrCard);

            assertThat(gqs.canActivateManaAbility(gd, myr)).isTrue();
        }

        @Test
        @DisplayName("returns false when own Stony Silence locks own artifacts")
        void returnsFalse_whenOwnStonySilenceLocks() {
            // Stony Silence on same player's battlefield still locks
            Card stonySilence = createEnchantmentWithStaticEffect("Stony Silence",
                    new ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(new PermanentIsArtifactPredicate()));
            addPermanent(player1Id, stonySilence);

            Card myrCard = createArtifactCreature("Leaden Myr", 1, 1, List.of(CardSubtype.MYR));
            Permanent myr = addPermanent(player1Id, myrCard);

            assertThat(gqs.canActivateManaAbility(gd, myr)).isFalse();
        }

        @Test
        @DisplayName("returns false when permanent has losesAllAbilitiesUntilEndOfTurn set")
        void returnsFalse_whenLosesAllAbilitiesUntilEndOfTurn() {
            Card myrCard = createArtifactCreature("Alloy Myr", 2, 2, List.of(CardSubtype.MYR));
            Permanent myr = addPermanent(player1Id, myrCard);
            myr.setLosesAllAbilitiesUntilEndOfTurn(true);

            assertThat(gqs.canActivateManaAbility(gd, myr)).isFalse();
        }

        @Test
        @DisplayName("returns true when losesAllAbilitiesUntilEndOfTurn is not set")
        void returnsTrue_whenLosesAllAbilitiesNotSet() {
            Card myrCard = createArtifactCreature("Alloy Myr", 2, 2, List.of(CardSubtype.MYR));
            Permanent myr = addPermanent(player1Id, myrCard);

            assertThat(gqs.canActivateManaAbility(gd, myr)).isTrue();
        }

        @Test
        @DisplayName("returns false when static LosesAllAbilitiesEffect applies via aura")
        void returnsFalse_whenStaticLosesAllAbilitiesFromAura() {
            Card myrCard = createArtifactCreature("Alloy Myr", 2, 2, List.of(CardSubtype.MYR));
            Permanent myr = addPermanent(player1Id, myrCard);

            // Deep Freeze-style aura that removes all abilities
            LosesAllAbilitiesEffect loseAll = new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE);
            Card auraCard = createAura("Deep Freeze", loseAll);
            // Stub the static handler to mimic real LosesAllAbilitiesEffectHandler behavior:
            // the enchanted creature loses all abilities (resolved via the CR 613 layered pass).
            when(staticEffectRegistry.getHandler(loseAll)).thenReturn((ctx, eff, acc) -> {
                if (ctx.source().isAttached()
                        && ctx.source().getAttachedTo().equals(ctx.target().getId())) {
                    acc.setLosesAllAbilities(true);
                }
            });
            Permanent aura = addPermanent(player1Id, auraCard);
            aura.setAttachedTo(myr.getId());

            assertThat(gqs.canActivateManaAbility(gd, myr)).isFalse();
        }
    }

    // ===== getOverriddenLandManaColor =====

    @Nested
    @DisplayName("getOverriddenLandManaColor")
    class GetOverriddenLandManaColor {

        @Test
        @DisplayName("returns null when land has no aura attached")
        void returnsNull_noAura() {
            Permanent land = addPermanent(player1Id, createLand("Plains"));

            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isNull();
        }

        @Test
        @DisplayName("returns BLACK when Evil Presence (Swamp type) is attached")
        void returnsBlack_evilPresenceAttached() {
            Permanent land = addPermanent(player1Id, createLand("Plains"));

            Card evilPresence = createAura("Evil Presence",
                    new EnchantedPermanentBecomesTypeEffect(CardSubtype.SWAMP));
            Permanent aura = addPermanent(player1Id, evilPresence);
            aura.setAttachedTo(land.getId());

            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isEqualTo(ManaColor.BLACK);
        }

        @Test
        @DisplayName("returns correct color for each basic land subtype override")
        void returnsCorrectColor_forEachBasicSubtype() {
            Permanent land = addPermanent(player1Id, createLand("Forest"));

            Card aura = createAura("Type Changer",
                    new EnchantedPermanentBecomesTypeEffect(CardSubtype.ISLAND));
            Permanent auraPerm = addPermanent(player1Id, aura);
            auraPerm.setAttachedTo(land.getId());
            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isEqualTo(ManaColor.BLUE);

            // Replace with Mountain
            aura = createAura("Type Changer 2",
                    new EnchantedPermanentBecomesTypeEffect(CardSubtype.MOUNTAIN));
            Permanent auraPerm2 = addPermanent(player1Id, aura);
            auraPerm.setAttachedTo(null); // detach old
            auraPerm2.setAttachedTo(land.getId());
            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isEqualTo(ManaColor.RED);
        }

        @Test
        @DisplayName("returns chosen subtype color for EnchantedPermanentBecomesChosenTypeEffect")
        void returnsChosenColor_convincingMirage() {
            Permanent land = addPermanent(player1Id, createLand("Mountain"));

            Card mirage = createAura("Convincing Mirage",
                    new EnchantedPermanentBecomesChosenTypeEffect());
            Permanent auraPerm = addPermanent(player1Id, mirage);
            auraPerm.setAttachedTo(land.getId());
            auraPerm.setChosenSubtype(CardSubtype.PLAINS);

            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isEqualTo(ManaColor.WHITE);
        }

        @Test
        @DisplayName("returns null for EnchantedPermanentBecomesChosenTypeEffect with no chosen subtype")
        void returnsNull_chosenTypeButNoSubtypeChosen() {
            Permanent land = addPermanent(player1Id, createLand("Mountain"));

            Card mirage = createAura("Convincing Mirage",
                    new EnchantedPermanentBecomesChosenTypeEffect());
            Permanent auraPerm = addPermanent(player1Id, mirage);
            auraPerm.setAttachedTo(land.getId());
            // chosenSubtype not set

            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isNull();
        }

        @Test
        @DisplayName("detects aura on opponent's battlefield attached to player's land")
        void detectsOpponentAura_attachedToPlayersLand() {
            Permanent land = addPermanent(player1Id, createLand("Forest"));

            Card evilPresence = createAura("Evil Presence",
                    new EnchantedPermanentBecomesTypeEffect(CardSubtype.SWAMP));
            Permanent aura = addPermanent(player2Id, evilPresence);
            aura.setAttachedTo(land.getId());

            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isEqualTo(ManaColor.BLACK);
        }

        @Test
        @DisplayName("returns null when aura is attached to a different permanent")
        void returnsNull_auraAttachedToDifferentPermanent() {
            Permanent land = addPermanent(player1Id, createLand("Plains"));
            Permanent otherLand = addPermanent(player1Id, createLand("Forest"));

            Card evilPresence = createAura("Evil Presence",
                    new EnchantedPermanentBecomesTypeEffect(CardSubtype.SWAMP));
            Permanent aura = addPermanent(player1Id, evilPresence);
            aura.setAttachedTo(otherLand.getId());

            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isNull();
            assertThat(gqs.getOverriddenLandManaColor(gd, otherLand)).isEqualTo(ManaColor.BLACK);
        }

        @Test
        @DisplayName("of two land-type-setting auras, the later timestamp wins (CR 613.7)")
        void laterLandTypeSetterWins() {
            Permanent land = addPermanent(player1Id, createLand("Forest"));

            Permanent first = addPermanent(player1Id, createAura("Sea's Claim",
                    new EnchantedPermanentBecomesTypeEffect(CardSubtype.ISLAND)));
            first.setAttachedTo(land.getId());
            Permanent second = addPermanent(player1Id, createAura("Evil Presence",
                    new EnchantedPermanentBecomesTypeEffect(CardSubtype.SWAMP)));
            second.setAttachedTo(land.getId());

            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isEqualTo(ManaColor.BLACK);
        }

        @Test
        @DisplayName("a later-entering Blood Moon effect beats an earlier land-type aura (CR 613.7)")
        void bloodMoonAfterAuraWins() {
            Permanent land = addPermanent(player1Id, createLand("Nonbasic Land"));
            Permanent aura = addPermanent(player1Id, createAura("Sea's Claim",
                    new EnchantedPermanentBecomesTypeEffect(CardSubtype.ISLAND)));
            aura.setAttachedTo(land.getId());

            Card bloodMoon = createEnchantment("Blood Moon");
            bloodMoon.addEffect(EffectSlot.STATIC, new NonbasicLandsBecomeTypeEffect(CardSubtype.MOUNTAIN));
            addPermanent(player2Id, bloodMoon);

            assertThat(gqs.getOverriddenLandManaColor(gd, land)).isEqualTo(ManaColor.RED);
        }
    }

    @Nested
    @DisplayName("playerHasProtectionFromColor")
    class PlayerHasProtectionFromColor {

        @Test
        @DisplayName("Returns true only for a color the player is protected from")
        void returnsTrueForProtectedColor() {
            gd.playerProtectionFromColorsUntilEndOfTurn
                    .computeIfAbsent(player1Id, k -> new HashSet<>()).add(CardColor.RED);

            assertThat(gqs.playerHasProtectionFromColor(gd, player1Id, CardColor.RED)).isTrue();
            assertThat(gqs.playerHasProtectionFromColor(gd, player1Id, CardColor.BLUE)).isFalse();
        }

        @Test
        @DisplayName("Returns false for a player with no protection and for a null color")
        void returnsFalseWhenNoProtection() {
            assertThat(gqs.playerHasProtectionFromColor(gd, player2Id, CardColor.RED)).isFalse();
            assertThat(gqs.playerHasProtectionFromColor(gd, player1Id, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getEffectiveColors")
    class GetEffectiveColors {

        @Test
        @DisplayName("Returns every intrinsic color of a multicolored permanent")
        void returnsAllIntrinsicColorsForMulticolor() {
            Card card = createCreature("Thistledown Liege", 1, 3, CardColor.WHITE);
            card.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
            Permanent perm = addPermanent(player1Id, card);

            assertThat(gqs.getEffectiveColors(gd, perm))
                    .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        }

        @Test
        @DisplayName("Falls back to the single color when the intrinsic list is empty")
        void fallsBackToSingleColor() {
            Permanent perm = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));

            assertThat(gqs.getEffectiveColors(gd, perm)).containsExactly(CardColor.GREEN);
        }
    }
}
