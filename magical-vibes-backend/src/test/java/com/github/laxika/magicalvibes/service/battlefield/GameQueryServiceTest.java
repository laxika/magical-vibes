package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetedBySpellColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerShroudEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
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
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameQueryServiceTest {

    @Mock
    private StaticEffectHandlerRegistry staticEffectRegistry;

    @InjectMocks
    private GameQueryService gqs;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
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
            perm.setAwakeningCounters(1);

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
            perm.setPlusOnePlusOneCounters(2);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
        }

        @Test
        @DisplayName("includes -1/-1 counters")
        void includesMinusCounters() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setMinusOneMinusOneCounters(1);

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
            // Stub the static handler to mimic real StaticEffectResolutionService behavior
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
            Permanent perm = addPermanent(player1Id, createCreatureWithStaticEffect("Karplusan Strider", 3, 4, CardColor.GREEN, new CantBeTargetedBySpellColorsEffect(EnumSet.of(CardColor.BLUE, CardColor.BLACK))));

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, null)).isFalse();
        }

        @Test
        @DisplayName("returns true when matching color protection exists")
        void returnsTrueWithMatchingColor() {
            Permanent perm = addPermanent(player1Id, createCreatureWithStaticEffect("Karplusan Strider", 3, 4, CardColor.GREEN, new CantBeTargetedBySpellColorsEffect(EnumSet.of(CardColor.BLUE, CardColor.BLACK))));

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.BLUE)).isTrue();
            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.BLACK)).isTrue();
        }

        @Test
        @DisplayName("returns false for non-matching color")
        void returnsFalseForNonMatchingColor() {
            Permanent perm = addPermanent(player1Id, createCreatureWithStaticEffect("Karplusan Strider", 3, 4, CardColor.GREEN, new CantBeTargetedBySpellColorsEffect(EnumSet.of(CardColor.BLUE, CardColor.BLACK))));

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.RED)).isFalse();
        }

        @Test
        @DisplayName("returns true when granted by static effect from another permanent")
        void returnsTrueWhenGrantedByStaticEffect() {
            addLordWithHandler(player1Id,
                    (ctx, eff, acc) -> acc.addGrantedEffect(
                            new CantBeTargetedBySpellColorsEffect(EnumSet.of(CardColor.BLUE))));
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

    // ===== matchesCardPredicate =====

    @Nested
    @DisplayName("matchesCardPredicate")
    class MatchesCardPredicate {

        @Test
        @DisplayName("null predicate returns true")
        void nullPredicateReturnsTrue() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.matchesCardPredicate(card, null, null)).isTrue();
        }

        @Test
        @DisplayName("CardTypePredicate matches primary type")
        void cardTypePredicateMatchesPrimaryType() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.matchesCardPredicate(card, new CardTypePredicate(CardType.CREATURE), null)).isTrue();
            assertThat(gqs.matchesCardPredicate(card, new CardTypePredicate(CardType.ARTIFACT), null)).isFalse();
        }

        @Test
        @DisplayName("CardTypePredicate matches additional type")
        void cardTypePredicateMatchesAdditionalType() {
            Card card = createArtifactCreature("Myr Sire", 1, 1, List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR));

            assertThat(gqs.matchesCardPredicate(card, new CardTypePredicate(CardType.ARTIFACT), null)).isTrue();
        }

        @Test
        @DisplayName("CardSubtypePredicate matches subtype")
        void cardSubtypePredicateMatches() {
            Card card = createCreature("Elf", 1, 1, CardColor.GREEN);
            card.setSubtypes(List.of(CardSubtype.ELF));

            assertThat(gqs.matchesCardPredicate(card, new CardSubtypePredicate(CardSubtype.ELF), null)).isTrue();
            assertThat(gqs.matchesCardPredicate(card, new CardSubtypePredicate(CardSubtype.GOBLIN), null)).isFalse();
        }

        @Test
        @DisplayName("CardKeywordPredicate matches keyword")
        void cardKeywordPredicateMatches() {
            Card card = createMirranCrusader();

            assertThat(gqs.matchesCardPredicate(card, new CardKeywordPredicate(Keyword.DOUBLE_STRIKE), null)).isTrue();
            assertThat(gqs.matchesCardPredicate(card, new CardKeywordPredicate(Keyword.FLYING), null)).isFalse();
        }

        @Test
        @DisplayName("CardIsSelfPredicate matches source card")
        void cardIsSelfPredicateMatches() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.matchesCardPredicate(card, new CardIsSelfPredicate(), card.getId())).isTrue();
            assertThat(gqs.matchesCardPredicate(card, new CardIsSelfPredicate(), UUID.randomUUID())).isFalse();
            assertThat(gqs.matchesCardPredicate(card, new CardIsSelfPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("CardColorPredicate matches card color")
        void cardColorPredicateMatches() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.matchesCardPredicate(card, new CardColorPredicate(CardColor.GREEN), null)).isTrue();
            assertThat(gqs.matchesCardPredicate(card, new CardColorPredicate(CardColor.RED), null)).isFalse();
        }

        @Test
        @DisplayName("CardIsAuraPredicate matches aura cards")
        void cardIsAuraPredicateMatches() {
            Card aura = createAura("Heart of Light", new PreventAllDamageToAndByEnchantedCreatureEffect());
            Card nonAura = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.matchesCardPredicate(aura, new CardIsAuraPredicate(), null)).isTrue();
            assertThat(gqs.matchesCardPredicate(nonAura, new CardIsAuraPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("CardNotPredicate negates")
        void cardNotPredicateNegates() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.matchesCardPredicate(card, new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), null)).isFalse();
            assertThat(gqs.matchesCardPredicate(card, new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT)), null)).isTrue();
        }

        @Test
        @DisplayName("CardAllOfPredicate requires all sub-predicates")
        void cardAllOfPredicateRequiresAll() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.matchesCardPredicate(card, new CardAllOfPredicate(List.of(
                    new CardTypePredicate(CardType.CREATURE),
                    new CardColorPredicate(CardColor.GREEN)
            )), null)).isTrue();

            assertThat(gqs.matchesCardPredicate(card, new CardAllOfPredicate(List.of(
                    new CardTypePredicate(CardType.CREATURE),
                    new CardColorPredicate(CardColor.RED)
            )), null)).isFalse();
        }

        @Test
        @DisplayName("CardAnyOfPredicate requires any sub-predicate")
        void cardAnyOfPredicateRequiresAny() {
            Card card = createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR));

            assertThat(gqs.matchesCardPredicate(card, new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.ARTIFACT),
                    new CardColorPredicate(CardColor.GREEN)
            )), null)).isTrue();

            assertThat(gqs.matchesCardPredicate(card, new CardAnyOfPredicate(List.of(
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

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsCreaturePredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsCreaturePredicate rejects non-creature")
        void creaturePredicateRejectsNonCreature() {
            Permanent perm = addPermanent(player1Id, createLand("Forest"));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsCreaturePredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsLandPredicate matches land")
        void landPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createLand("Forest"));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsLandPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsLandPredicate rejects non-land")
        void landPredicateRejectsNonLand() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsLandPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsArtifactPredicate matches artifact")
        void artifactPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createArtifact("Angel's Feather"));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsArtifactPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsArtifactPredicate rejects non-artifact")
        void artifactPredicateRejectsNonArtifact() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsArtifactPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsEnchantmentPredicate matches enchantment")
        void enchantmentPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createEnchantmentWithStaticEffect("Furnace of Rath", new DoubleDamageEffect()));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsEnchantmentPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsEnchantmentPredicate rejects non-enchantment")
        void enchantmentPredicateRejectsNonEnchantment() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsEnchantmentPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsTappedPredicate matches tapped permanent")
        void tappedPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.tap();

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsTappedPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsTappedPredicate rejects untapped permanent")
        void tappedPredicateRejectsUntapped() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsTappedPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsTokenPredicate matches token")
        void tokenPredicateMatches() {
            Card tokenCard = createCreature("Soldier Token", 1, 1, CardColor.WHITE);
            tokenCard.setToken(true);
            Permanent perm = addPermanent(player1Id, tokenCard);

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsTokenPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsTokenPredicate rejects non-token")
        void tokenPredicateRejectsNonToken() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsTokenPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsAttackingPredicate matches attacking creature")
        void attackingPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setAttacking(true);

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsAttackingPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsAttackingPredicate rejects non-attacking")
        void attackingPredicateRejectsNonAttacking() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsAttackingPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsBlockingPredicate matches blocking creature")
        void blockingPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setBlocking(true);

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsBlockingPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsBlockingPredicate rejects non-blocking")
        void blockingPredicateRejectsNonBlocking() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsBlockingPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate matches subtype")
        void subtypePredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.BEAR))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate rejects non-matching subtype")
        void subtypePredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.ELF))).isFalse();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate matches changeling for creature subtypes")
        void subtypePredicateMatchesChangeling() {
            Permanent perm = addPermanent(player1Id, createChangelingCreature("Changeling Wayfinder"));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.ELF))).isTrue();
            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate changeling does not match non-creature subtypes")
        void changelingDoesNotMatchNonCreatureSubtypes() {
            Permanent perm = addPermanent(player1Id, createChangelingCreature("Changeling Wayfinder"));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))).isFalse();
            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.AURA))).isFalse();
        }

        @Test
        @DisplayName("PermanentHasAnySubtypePredicate matches any matching subtype")
        void hasAnySubtypePredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentHasAnySubtypePredicate(EnumSet.of(CardSubtype.BEAR, CardSubtype.ELF)))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasAnySubtypePredicate rejects when no subtypes match")
        void hasAnySubtypePredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentHasAnySubtypePredicate(EnumSet.of(CardSubtype.ELF, CardSubtype.GOBLIN)))).isFalse();
        }

        @Test
        @DisplayName("PermanentHasKeywordPredicate matches keyword")
        void keywordPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createMirranCrusader());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasKeywordPredicate(Keyword.DOUBLE_STRIKE))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasKeywordPredicate rejects non-matching keyword")
        void keywordPredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasKeywordPredicate(Keyword.FLYING))).isFalse();
        }

        @Test
        @DisplayName("PermanentPowerAtMostPredicate matches when power is at or below threshold")
        void powerAtMostPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR))); // power 2

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentPowerAtMostPredicate(2))).isTrue();
            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentPowerAtMostPredicate(3))).isTrue();
        }

        @Test
        @DisplayName("PermanentPowerAtMostPredicate rejects when power exceeds threshold")
        void powerAtMostPredicateRejectsAboveThreshold() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR))); // power 2

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentPowerAtMostPredicate(1))).isFalse();
        }

        @Test
        @DisplayName("PermanentColorInPredicate matches color")
        void colorInPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.GREEN)))).isTrue();
        }

        @Test
        @DisplayName("PermanentColorInPredicate rejects non-matching color")
        void colorInPredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.RED)))).isFalse();
        }

        @Test
        @DisplayName("PermanentColorInPredicate matches overridden color")
        void colorInPredicateMatchesOverriddenColor() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            perm.setColorOverridden(true);
            perm.getTransientColors().add(CardColor.BLUE);

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.BLUE)))).isTrue();
            // Original color should not match when overridden
            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.GREEN)))).isFalse();
        }

        @Test
        @DisplayName("PermanentDealtDamageThisTurnPredicate matches permanent dealt damage this turn")
        void dealtDamageThisTurnPredicateMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            gd.permanentsDealtDamageThisTurn.add(perm.getId());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentDealtDamageThisTurnPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentDealtDamageThisTurnPredicate rejects permanent not dealt damage this turn")
        void dealtDamageThisTurnPredicateRejects() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentDealtDamageThisTurnPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentDealtDamageThisTurnPredicate returns false when gameData is null")
        void dealtDamageThisTurnPredicateReturnsFalseWithNullGameData() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            gd.permanentsDealtDamageThisTurn.add(perm.getId());

            assertThat(gqs.matchesPermanentPredicate(perm, new PermanentDealtDamageThisTurnPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("PermanentTruePredicate always returns true")
        void truePredicateAlwaysTrue() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentTruePredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentNotPredicate negates")
        void notPredicateNegates() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentNotPredicate(new PermanentIsCreaturePredicate()))).isFalse();
            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentNotPredicate(new PermanentIsArtifactPredicate()))).isTrue();
        }

        @Test
        @DisplayName("PermanentAllOfPredicate requires all")
        void allOfPredicateRequiresAll() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentHasSubtypePredicate(CardSubtype.BEAR)
            )))).isTrue();

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentIsArtifactPredicate()
            )))).isFalse();
        }

        @Test
        @DisplayName("PermanentAnyOfPredicate requires any")
        void anyOfPredicateRequiresAny() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentAnyOfPredicate(List.of(
                    new PermanentIsArtifactPredicate(),
                    new PermanentIsCreaturePredicate()
            )))).isTrue();

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentAnyOfPredicate(List.of(
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

            assertThat(gqs.matchesPermanentPredicate(perm, new PermanentIsSourceCardPredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentIsSourceCardPredicate rejects different card")
        void sourceCardPredicateRejectsDifferent() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(UUID.randomUUID());

            assertThat(gqs.matchesPermanentPredicate(perm, new PermanentIsSourceCardPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentControlledBySourceControllerPredicate matches controlled permanent")
        void controlledBySourceControllerMatches() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(gqs.matchesPermanentPredicate(perm, new PermanentControlledBySourceControllerPredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentControlledBySourceControllerPredicate rejects opponent's permanent")
        void controlledBySourceControllerRejectsOpponent() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(gqs.matchesPermanentPredicate(perm, new PermanentControlledBySourceControllerPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentIsPlaneswalkerPredicate rejects non-planeswalker")
        void planeswalkerPredicateRejectsNonPlaneswalker() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsPlaneswalkerPredicate())).isFalse();
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

            assertThat(gqs.matchesFilters(gd, perm, Set.of(
                    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be creature")
            ))).isTrue();
        }

        @Test
        @DisplayName("PermanentPredicateTargetFilter fails when predicate doesn't match")
        void permanentFilterFails() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThat(gqs.matchesFilters(gd, perm, Set.of(
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Must be artifact")
            ))).isFalse();
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter passes for controlled permanent")
        void controlledFilterPasses() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(gqs.matchesFilters(perm, Set.of(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature")
            ), ctx)).isTrue();
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter fails for opponent's permanent")
        void controlledFilterFailsForOpponent() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThat(gqs.matchesFilters(perm, Set.of(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature")
            ), ctx)).isFalse();
        }

        @Test
        @DisplayName("multiple filters must all match")
        void multipleFiltersMustAllMatch() {
            Permanent perm = addPermanent(player1Id, createArtifactCreature("Myr Sire", 1, 1, List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR)));

            assertThat(gqs.matchesFilters(gd, perm, Set.of(
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

            gqs.validateTargetFilter(gd,
                    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be creature"),
                    perm);
        }

        @Test
        @DisplayName("PermanentPredicateTargetFilter throws when doesn't match")
        void permanentFilterThrows() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThatThrownBy(() -> gqs.validateTargetFilter(gd,
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

            gqs.validateTargetFilter(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature"),
                    perm, ctx);
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter throws when not controlled")
        void controlledFilterThrowsWhenNotControlled() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThatThrownBy(() -> gqs.validateTargetFilter(
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

            gqs.validateTargetFilter(
                    new OwnedPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be owned creature"),
                    perm, ctx);
        }

        @Test
        @DisplayName("OwnedPermanentPredicateTargetFilter throws when not owned")
        void ownedFilterThrowsWhenNotOwned() {
            Permanent perm = addPermanent(player2Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1Id);

            assertThatThrownBy(() -> gqs.validateTargetFilter(
                    new OwnedPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be owned creature"),
                    perm, ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must be owned creature");
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter throws when gameData is null")
        void controlledFilterThrowsWithNullGameData() {
            Permanent perm = addPermanent(player1Id, createCreatureWithSubtypes("Grizzly Bears", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR)));

            assertThatThrownBy(() -> gqs.validateTargetFilter(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Error"),
                    perm, FilterContext.empty()))
                    .isInstanceOf(IllegalStateException.class);
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
}
