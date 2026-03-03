package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.c.ChangelingWayfinder;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.GaeasHerald;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartOfLight;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KarplusanStrider;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.m.MelirasKeepers;
import com.github.laxika.magicalvibes.cards.m.MirranCrusader;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
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
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameQueryServiceTest extends BaseCardTest {

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

    private static Card createArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{1}");
        return card;
    }

    private static Card createEnchantment(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{1}");
        card.setColor(color);
        return card;
    }

    private Permanent addPermanent(UUID playerId, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    // ===== findPermanentById =====

    @Nested
    @DisplayName("findPermanentById")
    class FindPermanentById {

        @Test
        @DisplayName("finds permanent on player1's battlefield")
        void findOnPlayer1Battlefield() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.findPermanentById(gd, perm.getId())).isSameAs(perm);
        }

        @Test
        @DisplayName("finds permanent on player2's battlefield")
        void findOnPlayer2Battlefield() {
            Permanent perm = addPermanent(player2.getId(), new GrizzlyBears());

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
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.findPermanentController(gd, perm.getId())).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("returns player2 for permanent on player2's battlefield")
        void returnsPlayer2() {
            Permanent perm = addPermanent(player2.getId(), new GrizzlyBears());

            assertThat(gqs.findPermanentController(gd, perm.getId())).isEqualTo(player2.getId());
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
            Card card = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(card));

            assertThat(gqs.findCardInGraveyardById(gd, card.getId())).isSameAs(card);
        }

        @Test
        @DisplayName("finds card in player2's graveyard")
        void findInPlayer2Graveyard() {
            Card card = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(card));

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
            Card card = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(card));

            assertThat(gqs.findGraveyardOwnerById(gd, card.getId())).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("returns player2 for card in player2's graveyard")
        void returnsPlayer2() {
            Card card = new GrizzlyBears();
            harness.setGraveyard(player2, List.of(card));

            assertThat(gqs.findGraveyardOwnerById(gd, card.getId())).isEqualTo(player2.getId());
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
            assertThat(gqs.getOpponentId(gd, player1.getId())).isEqualTo(player2.getId());
        }

        @Test
        @DisplayName("returns player1 when given player2")
        void returnsPlayer1ForPlayer2() {
            assertThat(gqs.getOpponentId(gd, player2.getId())).isEqualTo(player1.getId());
        }
    }

    // ===== getPriorityPlayerId =====

    @Nested
    @DisplayName("getPriorityPlayerId")
    class GetPriorityPlayerId {

        @Test
        @DisplayName("returns active player when neither has passed")
        void returnsActivePlayer() {
            gd.activePlayerId = player1.getId();
            gd.priorityPassedBy.clear();

            assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("returns non-active player when active has passed")
        void returnsNonActiveWhenActivePassed() {
            gd.activePlayerId = player1.getId();
            gd.priorityPassedBy.clear();
            gd.priorityPassedBy.add(player1.getId());

            assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player2.getId());
        }

        @Test
        @DisplayName("returns null when both have passed")
        void returnsNullWhenBothPassed() {
            gd.activePlayerId = player1.getId();
            gd.priorityPassedBy.clear();
            gd.priorityPassedBy.add(player1.getId());
            gd.priorityPassedBy.add(player2.getId());

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
            assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isTrue();
        }

        @Test
        @DisplayName("returns false when PlatinumEmperion is on battlefield")
        void returnsFalseWithPlatinumEmperion() {
            addPermanent(player1.getId(), new PlatinumEmperion());

            assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("only affects the controller, not the opponent")
        void onlyAffectsController() {
            addPermanent(player1.getId(), new PlatinumEmperion());

            assertThat(gqs.canPlayerLifeChange(gd, player2.getId())).isTrue();
        }
    }

    // ===== canPlayerLoseGame =====

    @Nested
    @DisplayName("canPlayerLoseGame")
    class CanPlayerLoseGame {

        @Test
        @DisplayName("returns true by default")
        void returnsTrueByDefault() {
            assertThat(gqs.canPlayerLoseGame(gd, player1.getId())).isTrue();
        }

        @Test
        @DisplayName("returns false when PlatinumAngel is on battlefield")
        void returnsFalseWithPlatinumAngel() {
            addPermanent(player1.getId(), new PlatinumAngel());

            assertThat(gqs.canPlayerLoseGame(gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("only affects the controller, not the opponent")
        void onlyAffectsController() {
            addPermanent(player1.getId(), new PlatinumAngel());

            assertThat(gqs.canPlayerLoseGame(gd, player2.getId())).isTrue();
        }
    }

    // ===== isCreature =====

    @Nested
    @DisplayName("isCreature")
    class IsCreature {

        @Test
        @DisplayName("returns true for a creature")
        void returnsTrueForCreature() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.isCreature(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns false for a non-creature")
        void returnsFalseForNonCreature() {
            Permanent perm = addPermanent(player1.getId(), new Forest());

            assertThat(gqs.isCreature(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true for animated permanent")
        void returnsTrueForAnimated() {
            Permanent perm = addPermanent(player1.getId(), new Forest());
            perm.setAnimatedUntilEndOfTurn(true);
            perm.setAnimatedPower(2);
            perm.setAnimatedToughness(2);

            assertThat(gqs.isCreature(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true for permanent with awakening counters")
        void returnsTrueForAwakened() {
            Permanent perm = addPermanent(player1.getId(), new Forest());
            perm.setAwakeningCounters(1);

            assertThat(gqs.isCreature(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true for artifact with animate artifact effect on battlefield")
        void returnsTrueForArtifactWithAnimateEffect() {
            addPermanent(player1.getId(), new MarchOfTheMachines());
            Permanent artifact = addPermanent(player1.getId(), new AngelsFeather());

            assertThat(gqs.isCreature(gd, artifact)).isTrue();
        }

        @Test
        @DisplayName("returns true for artifact creature")
        void returnsTrueForArtifactCreature() {
            Permanent perm = addPermanent(player1.getId(), new MyrSire());

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
            Permanent perm = addPermanent(player1.getId(), new AngelsFeather());

            assertThat(gqs.isArtifact(perm)).isTrue();
        }

        @Test
        @DisplayName("returns false for non-artifact")
        void returnsFalseForNonArtifact() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.isArtifact(perm)).isFalse();
        }

        @Test
        @DisplayName("returns true for artifact creature (additional type)")
        void returnsTrueForArtifactCreature() {
            Permanent perm = addPermanent(player1.getId(), new MyrSire());

            assertThat(gqs.isArtifact(perm)).isTrue();
        }

        @Test
        @DisplayName("returns true for permanent with granted artifact type")
        void returnsTrueForGrantedType() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.getGrantedCardTypes().add(CardType.ARTIFACT);

            assertThat(gqs.isArtifact(perm)).isTrue();
        }
    }

    // ===== isMetalcraftMet =====

    @Nested
    @DisplayName("isMetalcraftMet")
    class IsMetalcraftMet {

        @Test
        @DisplayName("returns false with no artifacts")
        void returnsFalseWithNoArtifacts() {
            assertThat(gqs.isMetalcraftMet(gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("returns false with 2 artifacts")
        void returnsFalseWithTwoArtifacts() {
            addPermanent(player1.getId(), createArtifact("Artifact A"));
            addPermanent(player1.getId(), createArtifact("Artifact B"));

            assertThat(gqs.isMetalcraftMet(gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("returns true with exactly 3 artifacts")
        void returnsTrueWithThreeArtifacts() {
            addPermanent(player1.getId(), createArtifact("Artifact A"));
            addPermanent(player1.getId(), createArtifact("Artifact B"));
            addPermanent(player1.getId(), createArtifact("Artifact C"));

            assertThat(gqs.isMetalcraftMet(gd, player1.getId())).isTrue();
        }

        @Test
        @DisplayName("returns true with more than 3 artifacts")
        void returnsTrueWithMoreThanThree() {
            addPermanent(player1.getId(), createArtifact("Artifact A"));
            addPermanent(player1.getId(), createArtifact("Artifact B"));
            addPermanent(player1.getId(), createArtifact("Artifact C"));
            addPermanent(player1.getId(), createArtifact("Artifact D"));

            assertThat(gqs.isMetalcraftMet(gd, player1.getId())).isTrue();
        }

        @Test
        @DisplayName("does not count opponent's artifacts")
        void doesNotCountOpponentArtifacts() {
            addPermanent(player2.getId(), createArtifact("Artifact A"));
            addPermanent(player2.getId(), createArtifact("Artifact B"));
            addPermanent(player2.getId(), createArtifact("Artifact C"));

            assertThat(gqs.isMetalcraftMet(gd, player1.getId())).isFalse();
        }
    }

    // ===== hasKeyword =====

    @Nested
    @DisplayName("hasKeyword")
    class HasKeyword {

        @Test
        @DisplayName("returns true for innate keyword")
        void returnsTrueForInnateKeyword() {
            Permanent perm = addPermanent(player1.getId(), new MirranCrusader());

            assertThat(gqs.hasKeyword(gd, perm, Keyword.DOUBLE_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("returns false when keyword not present")
        void returnsFalseWhenAbsent() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.hasKeyword(gd, perm, Keyword.FLYING)).isFalse();
        }

        @Test
        @DisplayName("returns true for granted keyword")
        void returnsTrueForGrantedKeyword() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.getGrantedKeywords().add(Keyword.FLYING);

            assertThat(gqs.hasKeyword(gd, perm, Keyword.FLYING)).isTrue();
        }
    }

    // ===== cantHaveCounters =====

    @Nested
    @DisplayName("cantHaveCounters")
    class CantHaveCounters {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.cantHaveCounters(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true for permanent with CantHaveCountersEffect")
        void returnsTrueWithEffect() {
            Permanent perm = addPermanent(player1.getId(), new MelirasKeepers());

            assertThat(gqs.cantHaveCounters(gd, perm)).isTrue();
        }
    }

    // ===== hasCantBeBlocked =====

    @Nested
    @DisplayName("hasCantBeBlocked")
    class HasCantBeBlocked {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.hasCantBeBlocked(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true when permanent cantBeBlocked flag is set")
        void returnsTrueWhenFlagSet() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setCantBeBlocked(true);

            assertThat(gqs.hasCantBeBlocked(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true when card has CantBeBlockedEffect")
        void returnsTrueWithStaticEffect() {
            Permanent perm = addPermanent(player1.getId(), new PhantomWarrior());

            assertThat(gqs.hasCantBeBlocked(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true when attached equipment has CantBeBlockedEffect")
        void returnsTrueWithAttachedEquipment() {
            Permanent creature = addPermanent(player1.getId(), new GrizzlyBears());

            Card equipCard = createArtifact("Unblockable Cloak");
            equipCard.addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
            Permanent equipment = addPermanent(player1.getId(), equipCard);
            equipment.setAttachedTo(creature.getId());

            assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
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
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        }

        @Test
        @DisplayName("returns base toughness for vanilla creature")
        void baseToughness() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
        }

        @Test
        @DisplayName("includes power modifier")
        void includesPowerModifier() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setPowerModifier(3);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        }

        @Test
        @DisplayName("includes toughness modifier")
        void includesToughnessModifier() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setToughnessModifier(2);

            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
        }

        @Test
        @DisplayName("includes +1/+1 counters")
        void includesPlusCounters() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setPlusOnePlusOneCounters(2);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
        }

        @Test
        @DisplayName("includes -1/-1 counters")
        void includesMinusCounters() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setMinusOneMinusOneCounters(1);

            assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
        }
    }

    // ===== getEffectiveCombatDamage =====

    @Nested
    @DisplayName("getEffectiveCombatDamage")
    class GetEffectiveCombatDamage {

        @Test
        @DisplayName("returns power normally")
        void returnsPowerNormally() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

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
            Permanent perm = addPermanent(player1.getId(), new MirranCrusader());

            assertThat(gqs.hasProtectionFrom(gd, perm, null)).isFalse();
        }

        @Test
        @DisplayName("returns false when no protection from that color")
        void returnsFalseWhenNoProtection() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.BLACK)).isFalse();
        }

        @Test
        @DisplayName("returns true from ProtectionFromColorsEffect")
        void returnsTrueWithProtectionEffect() {
            Permanent perm = addPermanent(player1.getId(), new MirranCrusader());

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.BLACK)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.GREEN)).isTrue();
        }

        @Test
        @DisplayName("returns false for non-protected color")
        void returnsFalseForNonProtectedColor() {
            Permanent perm = addPermanent(player1.getId(), new MirranCrusader());

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.RED)).isFalse();
            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.WHITE)).isFalse();
            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.BLUE)).isFalse();
        }

        @Test
        @DisplayName("returns true from chosen color")
        void returnsTrueFromChosenColor() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setChosenColor(CardColor.RED);

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.RED)).isTrue();
        }

        @Test
        @DisplayName("returns false for chosen color that doesn't match")
        void returnsFalseFromNonMatchingChosenColor() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setChosenColor(CardColor.RED);

            assertThat(gqs.hasProtectionFrom(gd, perm, CardColor.BLUE)).isFalse();
        }
    }

    // ===== hasProtectionFromSourceCardTypes =====

    @Nested
    @DisplayName("hasProtectionFromSourceCardTypes")
    class HasProtectionFromSourceCardTypes {

        @Test
        @DisplayName("returns false when no card type protection")
        void returnsFalseWhenNoProtection() {
            Permanent target = addPermanent(player1.getId(), new GrizzlyBears());
            Permanent source = addPermanent(player2.getId(), new GrizzlyBears());

            assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, source)).isFalse();
        }

        @Test
        @DisplayName("returns true when source has protected card type")
        void returnsTrueWhenSourceMatchesProtection() {
            Permanent target = addPermanent(player1.getId(), new GrizzlyBears());
            target.getProtectionFromCardTypes().add(CardType.ARTIFACT);
            Permanent source = addPermanent(player2.getId(), new AngelsFeather());

            assertThat(gqs.hasProtectionFromSourceCardTypes(gd, target, source)).isTrue();
        }

        @Test
        @DisplayName("returns false when source does not match protected type")
        void returnsFalseWhenSourceDoesNotMatch() {
            Permanent target = addPermanent(player1.getId(), new GrizzlyBears());
            target.getProtectionFromCardTypes().add(CardType.ENCHANTMENT);
            Permanent source = addPermanent(player2.getId(), new AngelsFeather());

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
            Permanent target = addPermanent(player1.getId(), new GrizzlyBears());
            Permanent source = addPermanent(player2.getId(),
                    createCreature("Red Goblin", 1, 1, CardColor.RED));

            assertThat(gqs.hasProtectionFromSource(gd, target, source)).isFalse();
        }

        @Test
        @DisplayName("returns true when color protection matches")
        void returnsTrueFromColorProtection() {
            Permanent target = addPermanent(player1.getId(), new MirranCrusader());
            Permanent source = addPermanent(player2.getId(),
                    createCreature("Black Knight", 2, 2, CardColor.BLACK));

            assertThat(gqs.hasProtectionFromSource(gd, target, source)).isTrue();
        }

        @Test
        @DisplayName("returns true when card type protection matches")
        void returnsTrueFromCardTypeProtection() {
            Permanent target = addPermanent(player1.getId(), new GrizzlyBears());
            target.getProtectionFromCardTypes().add(CardType.ARTIFACT);
            Permanent source = addPermanent(player2.getId(), new AngelsFeather());

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
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.BLUE)).isFalse();
        }

        @Test
        @DisplayName("returns false for null spell color")
        void returnsFalseForNullColor() {
            Permanent perm = addPermanent(player1.getId(), new KarplusanStrider());

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, null)).isFalse();
        }

        @Test
        @DisplayName("returns true when matching color protection exists")
        void returnsTrueWithMatchingColor() {
            Permanent perm = addPermanent(player1.getId(), new KarplusanStrider());

            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.BLUE)).isTrue();
            assertThat(gqs.cantBeTargetedBySpellColor(gd, perm, CardColor.BLACK)).isTrue();
        }

        @Test
        @DisplayName("returns false for non-matching color")
        void returnsFalseForNonMatchingColor() {
            Permanent perm = addPermanent(player1.getId(), new KarplusanStrider());

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
            assertThat(gqs.playerHasShroud(gd, player1.getId())).isFalse();
        }

        @Test
        @DisplayName("returns true when TrueBeliever is on battlefield")
        void returnsTrueWithTrueBeliever() {
            addPermanent(player1.getId(), new TrueBeliever());

            assertThat(gqs.playerHasShroud(gd, player1.getId())).isTrue();
        }

        @Test
        @DisplayName("does not affect opponent")
        void doesNotAffectOpponent() {
            addPermanent(player1.getId(), new TrueBeliever());

            assertThat(gqs.playerHasShroud(gd, player2.getId())).isFalse();
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
            Card creature = new GrizzlyBears();

            assertThat(gqs.isUncounterable(gd, creature)).isFalse();
        }

        @Test
        @DisplayName("returns true when CreatureSpellsCantBeCounteredEffect on battlefield")
        void returnsTrueWithEffect() {
            addPermanent(player1.getId(), new GaeasHerald());
            Card creature = new GrizzlyBears();

            assertThat(gqs.isUncounterable(gd, creature)).isTrue();
        }

        @Test
        @DisplayName("works even if effect is on opponent's battlefield")
        void worksWithOpponentEffect() {
            addPermanent(player2.getId(), new GaeasHerald());
            Card creature = new GrizzlyBears();

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
            addPermanent(player1.getId(), new MarchOfTheMachines());

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
            addPermanent(player1.getId(), new FurnaceOfRath());

            assertThat(gqs.getDamageMultiplier(gd)).isEqualTo(2);
        }

        @Test
        @DisplayName("returns 4 with two Furnaces of Rath")
        void returnsFourWithTwoFurnaces() {
            addPermanent(player1.getId(), new FurnaceOfRath());
            addPermanent(player2.getId(), new FurnaceOfRath());

            assertThat(gqs.getDamageMultiplier(gd)).isEqualTo(4);
        }

        @Test
        @DisplayName("applyDamageMultiplier applies correctly")
        void applyMultiplier() {
            addPermanent(player1.getId(), new FurnaceOfRath());

            assertThat(gqs.applyDamageMultiplier(gd, 3)).isEqualTo(6);
        }

        @Test
        @DisplayName("applyDamageMultiplier with no effect returns same value")
        void applyMultiplierNoEffect() {
            assertThat(gqs.applyDamageMultiplier(gd, 3)).isEqualTo(3);
        }
    }

    // ===== isPreventedFromDealingDamage =====

    @Nested
    @DisplayName("isPreventedFromDealingDamage")
    class IsPreventedFromDealingDamage {

        @Test
        @DisplayName("returns false by default")
        void returnsFalseByDefault() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.isPreventedFromDealingDamage(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true when permanent is in prevention set")
        void returnsTrueWhenInPreventionSet() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            gd.permanentsPreventedFromDealingDamage.add(perm.getId());

            assertThat(gqs.isPreventedFromDealingDamage(gd, perm)).isTrue();
        }

        @Test
        @DisplayName("returns true when color damage prevention is active")
        void returnsTrueWithColorPrevention() {
            Card card = createCreature("Green Bear", 2, 2, CardColor.GREEN);
            Permanent perm = addPermanent(player1.getId(), card);
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
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.isEnchanted(gd, perm)).isFalse();
        }

        @Test
        @DisplayName("returns true when aura is attached")
        void returnsTrueWithAura() {
            Permanent creature = addPermanent(player1.getId(), new GrizzlyBears());
            Permanent aura = addPermanent(player1.getId(), new HeartOfLight());
            aura.setAttachedTo(creature.getId());

            assertThat(gqs.isEnchanted(gd, creature)).isTrue();
        }

        @Test
        @DisplayName("returns false when non-aura attachment (equipment)")
        void returnsFalseWithEquipment() {
            Permanent creature = addPermanent(player1.getId(), new GrizzlyBears());
            Permanent equipment = addPermanent(player1.getId(), createArtifact("Some Equipment"));
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
            Permanent creature = addPermanent(player1.getId(), new GrizzlyBears());
            Permanent aura = addPermanent(player1.getId(), new HeartOfLight());
            aura.setAttachedTo(creature.getId());

            assertThat(gqs.hasAuraWithEffect(gd, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)).isTrue();
        }

        @Test
        @DisplayName("returns false when no attachment")
        void returnsFalseWhenNoAttachment() {
            Permanent creature = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.hasAuraWithEffect(gd, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)).isFalse();
        }

        @Test
        @DisplayName("returns false when attached permanent has different effect")
        void returnsFalseWithDifferentEffect() {
            Permanent creature = addPermanent(player1.getId(), new GrizzlyBears());
            Permanent aura = addPermanent(player1.getId(), new HeartOfLight());
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
            Permanent explicitSource = addPermanent(player1.getId(), new MirranCrusader());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, new GrizzlyBears(), player1.getId(),
                    "Test", List.of());

            assertThat(gqs.sourceHasKeyword(gd, entry, explicitSource, Keyword.DOUBLE_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("falls back to entry source when no explicit source")
        void fallsBackToEntrySource() {
            Permanent source = addPermanent(player1.getId(), new MirranCrusader());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, new MirranCrusader(), player1.getId(),
                    "Test", List.of(), null, source.getId());

            assertThat(gqs.sourceHasKeyword(gd, entry, null, Keyword.DOUBLE_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("returns false when entry source permanent lacks keyword")
        void returnsFalseWhenEntrySourceLacksKeyword() {
            Permanent source = addPermanent(player1.getId(), new GrizzlyBears());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, new GrizzlyBears(), player1.getId(),
                    "Test", List.of(), null, source.getId());

            assertThat(gqs.sourceHasKeyword(gd, entry, null, Keyword.FLYING)).isFalse();
        }

        @Test
        @DisplayName("returns false when no source permanent")
        void returnsFalseWhenNoSource() {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, new GrizzlyBears(), player1.getId(),
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
            assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.ELF)).isEqualTo(0);
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

            addPermanent(player1.getId(), elf1);
            addPermanent(player1.getId(), elf2);
            addPermanent(player1.getId(), nonElf);

            assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.ELF)).isEqualTo(2);
        }

        @Test
        @DisplayName("does not count opponent's permanents")
        void doesNotCountOpponent() {
            Card elf = createCreature("Elf", 1, 1, CardColor.GREEN);
            elf.setSubtypes(List.of(CardSubtype.ELF));
            addPermanent(player2.getId(), elf);

            assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.ELF)).isEqualTo(0);
        }
    }

    // ===== matchesCardPredicate =====

    @Nested
    @DisplayName("matchesCardPredicate")
    class MatchesCardPredicate {

        @Test
        @DisplayName("null predicate returns true")
        void nullPredicateReturnsTrue() {
            Card card = new GrizzlyBears();

            assertThat(gqs.matchesCardPredicate(card, null, null)).isTrue();
        }

        @Test
        @DisplayName("CardTypePredicate matches primary type")
        void cardTypePredicateMatchesPrimaryType() {
            Card card = new GrizzlyBears();

            assertThat(gqs.matchesCardPredicate(card, new CardTypePredicate(CardType.CREATURE), null)).isTrue();
            assertThat(gqs.matchesCardPredicate(card, new CardTypePredicate(CardType.ARTIFACT), null)).isFalse();
        }

        @Test
        @DisplayName("CardTypePredicate matches additional type")
        void cardTypePredicateMatchesAdditionalType() {
            Card card = new MyrSire();

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
            Card card = new MirranCrusader();

            assertThat(gqs.matchesCardPredicate(card, new CardKeywordPredicate(Keyword.DOUBLE_STRIKE), null)).isTrue();
            assertThat(gqs.matchesCardPredicate(card, new CardKeywordPredicate(Keyword.FLYING), null)).isFalse();
        }

        @Test
        @DisplayName("CardIsSelfPredicate matches source card")
        void cardIsSelfPredicateMatches() {
            Card card = new GrizzlyBears();

            assertThat(gqs.matchesCardPredicate(card, new CardIsSelfPredicate(), card.getId())).isTrue();
            assertThat(gqs.matchesCardPredicate(card, new CardIsSelfPredicate(), UUID.randomUUID())).isFalse();
            assertThat(gqs.matchesCardPredicate(card, new CardIsSelfPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("CardColorPredicate matches card color")
        void cardColorPredicateMatches() {
            Card card = new GrizzlyBears();

            assertThat(gqs.matchesCardPredicate(card, new CardColorPredicate(CardColor.GREEN), null)).isTrue();
            assertThat(gqs.matchesCardPredicate(card, new CardColorPredicate(CardColor.RED), null)).isFalse();
        }

        @Test
        @DisplayName("CardIsAuraPredicate matches aura cards")
        void cardIsAuraPredicateMatches() {
            Card aura = new HeartOfLight();
            Card nonAura = new GrizzlyBears();

            assertThat(gqs.matchesCardPredicate(aura, new CardIsAuraPredicate(), null)).isTrue();
            assertThat(gqs.matchesCardPredicate(nonAura, new CardIsAuraPredicate(), null)).isFalse();
        }

        @Test
        @DisplayName("CardNotPredicate negates")
        void cardNotPredicateNegates() {
            Card card = new GrizzlyBears();

            assertThat(gqs.matchesCardPredicate(card, new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)), null)).isFalse();
            assertThat(gqs.matchesCardPredicate(card, new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT)), null)).isTrue();
        }

        @Test
        @DisplayName("CardAllOfPredicate requires all sub-predicates")
        void cardAllOfPredicateRequiresAll() {
            Card card = new GrizzlyBears();

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
            Card card = new GrizzlyBears();

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
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsCreaturePredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsCreaturePredicate rejects non-creature")
        void creaturePredicateRejectsNonCreature() {
            Permanent perm = addPermanent(player1.getId(), new Forest());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsCreaturePredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsLandPredicate matches land")
        void landPredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new Forest());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsLandPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsLandPredicate rejects non-land")
        void landPredicateRejectsNonLand() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsLandPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsArtifactPredicate matches artifact")
        void artifactPredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new AngelsFeather());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsArtifactPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsArtifactPredicate rejects non-artifact")
        void artifactPredicateRejectsNonArtifact() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsArtifactPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsEnchantmentPredicate matches enchantment")
        void enchantmentPredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new FurnaceOfRath());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsEnchantmentPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsEnchantmentPredicate rejects non-enchantment")
        void enchantmentPredicateRejectsNonEnchantment() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsEnchantmentPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsTappedPredicate matches tapped permanent")
        void tappedPredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.tap();

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsTappedPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsTappedPredicate rejects untapped permanent")
        void tappedPredicateRejectsUntapped() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsTappedPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsTokenPredicate matches token")
        void tokenPredicateMatches() {
            Card tokenCard = createCreature("Soldier Token", 1, 1, CardColor.WHITE);
            tokenCard.setToken(true);
            Permanent perm = addPermanent(player1.getId(), tokenCard);

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsTokenPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsTokenPredicate rejects non-token")
        void tokenPredicateRejectsNonToken() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsTokenPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsAttackingPredicate matches attacking creature")
        void attackingPredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setAttacking(true);

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsAttackingPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsAttackingPredicate rejects non-attacking")
        void attackingPredicateRejectsNonAttacking() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsAttackingPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentIsBlockingPredicate matches blocking creature")
        void blockingPredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setBlocking(true);

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsBlockingPredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentIsBlockingPredicate rejects non-blocking")
        void blockingPredicateRejectsNonBlocking() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentIsBlockingPredicate())).isFalse();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate matches subtype")
        void subtypePredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.BEAR))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate rejects non-matching subtype")
        void subtypePredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.ELF))).isFalse();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate matches changeling for creature subtypes")
        void subtypePredicateMatchesChangeling() {
            Permanent perm = addPermanent(player1.getId(), new ChangelingWayfinder());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.ELF))).isTrue();
            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasSubtypePredicate changeling does not match non-creature subtypes")
        void changelingDoesNotMatchNonCreatureSubtypes() {
            Permanent perm = addPermanent(player1.getId(), new ChangelingWayfinder());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT))).isFalse();
            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasSubtypePredicate(CardSubtype.AURA))).isFalse();
        }

        @Test
        @DisplayName("PermanentHasAnySubtypePredicate matches any matching subtype")
        void hasAnySubtypePredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentHasAnySubtypePredicate(EnumSet.of(CardSubtype.BEAR, CardSubtype.ELF)))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasAnySubtypePredicate rejects when no subtypes match")
        void hasAnySubtypePredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentHasAnySubtypePredicate(EnumSet.of(CardSubtype.ELF, CardSubtype.GOBLIN)))).isFalse();
        }

        @Test
        @DisplayName("PermanentHasKeywordPredicate matches keyword")
        void keywordPredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new MirranCrusader());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasKeywordPredicate(Keyword.DOUBLE_STRIKE))).isTrue();
        }

        @Test
        @DisplayName("PermanentHasKeywordPredicate rejects non-matching keyword")
        void keywordPredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentHasKeywordPredicate(Keyword.FLYING))).isFalse();
        }

        @Test
        @DisplayName("PermanentPowerAtMostPredicate matches when power is at or below threshold")
        void powerAtMostPredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears()); // power 2

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentPowerAtMostPredicate(2))).isTrue();
            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentPowerAtMostPredicate(3))).isTrue();
        }

        @Test
        @DisplayName("PermanentPowerAtMostPredicate rejects when power exceeds threshold")
        void powerAtMostPredicateRejectsAboveThreshold() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears()); // power 2

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentPowerAtMostPredicate(1))).isFalse();
        }

        @Test
        @DisplayName("PermanentColorInPredicate matches color")
        void colorInPredicateMatches() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.GREEN)))).isTrue();
        }

        @Test
        @DisplayName("PermanentColorInPredicate rejects non-matching color")
        void colorInPredicateRejectsNonMatching() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.RED)))).isFalse();
        }

        @Test
        @DisplayName("PermanentColorInPredicate matches overridden color")
        void colorInPredicateMatchesOverriddenColor() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            perm.setColorOverridden(true);
            perm.getGrantedColors().add(CardColor.BLUE);

            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.BLUE)))).isTrue();
            // Original color should not match when overridden
            assertThat(gqs.matchesPermanentPredicate(gd, perm,
                    new PermanentColorInPredicate(EnumSet.of(CardColor.GREEN)))).isFalse();
        }

        @Test
        @DisplayName("PermanentTruePredicate always returns true")
        void truePredicateAlwaysTrue() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentTruePredicate())).isTrue();
        }

        @Test
        @DisplayName("PermanentNotPredicate negates")
        void notPredicateNegates() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentNotPredicate(new PermanentIsCreaturePredicate()))).isFalse();
            assertThat(gqs.matchesPermanentPredicate(gd, perm, new PermanentNotPredicate(new PermanentIsArtifactPredicate()))).isTrue();
        }

        @Test
        @DisplayName("PermanentAllOfPredicate requires all")
        void allOfPredicateRequiresAll() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

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
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

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
            Card card = new GrizzlyBears();
            Permanent perm = addPermanent(player1.getId(), card);
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(card.getId());

            assertThat(gqs.matchesPermanentPredicate(perm, new PermanentIsSourceCardPredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentIsSourceCardPredicate rejects different card")
        void sourceCardPredicateRejectsDifferent() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            FilterContext ctx = FilterContext.of(gd).withSourceCardId(UUID.randomUUID());

            assertThat(gqs.matchesPermanentPredicate(perm, new PermanentIsSourceCardPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentControlledBySourceControllerPredicate matches controlled permanent")
        void controlledBySourceControllerMatches() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1.getId());

            assertThat(gqs.matchesPermanentPredicate(perm, new PermanentControlledBySourceControllerPredicate(), ctx)).isTrue();
        }

        @Test
        @DisplayName("PermanentControlledBySourceControllerPredicate rejects opponent's permanent")
        void controlledBySourceControllerRejectsOpponent() {
            Permanent perm = addPermanent(player2.getId(), new GrizzlyBears());
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1.getId());

            assertThat(gqs.matchesPermanentPredicate(perm, new PermanentControlledBySourceControllerPredicate(), ctx)).isFalse();
        }

        @Test
        @DisplayName("PermanentIsPlaneswalkerPredicate rejects non-planeswalker")
        void planeswalkerPredicateRejectsNonPlaneswalker() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

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
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesFilters(gd, perm, Set.of(
                    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be creature")
            ))).isTrue();
        }

        @Test
        @DisplayName("PermanentPredicateTargetFilter fails when predicate doesn't match")
        void permanentFilterFails() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThat(gqs.matchesFilters(gd, perm, Set.of(
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Must be artifact")
            ))).isFalse();
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter passes for controlled permanent")
        void controlledFilterPasses() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1.getId());

            assertThat(gqs.matchesFilters(perm, Set.of(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature")
            ), ctx)).isTrue();
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter fails for opponent's permanent")
        void controlledFilterFailsForOpponent() {
            Permanent perm = addPermanent(player2.getId(), new GrizzlyBears());
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1.getId());

            assertThat(gqs.matchesFilters(perm, Set.of(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature")
            ), ctx)).isFalse();
        }

        @Test
        @DisplayName("multiple filters must all match")
        void multipleFiltersMustAllMatch() {
            Permanent perm = addPermanent(player1.getId(), new MyrSire());

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
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            gqs.validateTargetFilter(gd,
                    new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be creature"),
                    perm);
        }

        @Test
        @DisplayName("PermanentPredicateTargetFilter throws when doesn't match")
        void permanentFilterThrows() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThatThrownBy(() -> gqs.validateTargetFilter(gd,
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Must be artifact"),
                    perm))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must be artifact");
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter passes for controlled permanent")
        void controlledFilterPasses() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1.getId());

            gqs.validateTargetFilter(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature"),
                    perm, ctx);
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter throws when not controlled")
        void controlledFilterThrowsWhenNotControlled() {
            Permanent perm = addPermanent(player2.getId(), new GrizzlyBears());
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1.getId());

            assertThatThrownBy(() -> gqs.validateTargetFilter(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be controlled creature"),
                    perm, ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must be controlled creature");
        }

        @Test
        @DisplayName("OwnedPermanentPredicateTargetFilter passes for owned permanent")
        void ownedFilterPasses() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1.getId());

            gqs.validateTargetFilter(
                    new OwnedPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be owned creature"),
                    perm, ctx);
        }

        @Test
        @DisplayName("OwnedPermanentPredicateTargetFilter throws when not owned")
        void ownedFilterThrowsWhenNotOwned() {
            Permanent perm = addPermanent(player2.getId(), new GrizzlyBears());
            FilterContext ctx = FilterContext.of(gd).withSourceControllerId(player1.getId());

            assertThatThrownBy(() -> gqs.validateTargetFilter(
                    new OwnedPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Must be owned creature"),
                    perm, ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Must be owned creature");
        }

        @Test
        @DisplayName("ControlledPermanentPredicateTargetFilter throws when gameData is null")
        void controlledFilterThrowsWithNullGameData() {
            Permanent perm = addPermanent(player1.getId(), new GrizzlyBears());

            assertThatThrownBy(() -> gqs.validateTargetFilter(
                    new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Error"),
                    perm, FilterContext.empty()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
