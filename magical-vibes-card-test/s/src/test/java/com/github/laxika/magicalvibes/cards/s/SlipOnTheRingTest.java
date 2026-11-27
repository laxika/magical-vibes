package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.w.WallOfEarth;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlipOnTheRing.class, GrizzlyBears.class, SauronTheDarkLord.class, Forest.class,
        DiabolicEdict.class, WallOfEarth.class})
class SlipOnTheRingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and returns a creature you own under your control, then tempts you by the Ring")
    void flickersOwnedCreatureAndTemptsByTheRing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID originalId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, originalId);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(gd.ringStates.get(player1.getId()).level()).isEqualTo(1);
        assertThat(gd.ringStates.get(player1.getId()).bearerId()).isEqualTo(returned.getId());
        assertThat(gqs.hasEffectiveSupertype(gd, returned, CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    @DisplayName("Triggers \"whenever the Ring tempts you\" abilities and logs the temptation")
    void triggersWheneverTheRingTemptsYouAbilities() {
        Permanent sauron = harness.addToBattlefieldAndReturn(player1, new SauronTheDarkLord());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.handlePermanentChosen(player1, sauron.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.ringBearerId(player1.getId())).isEqualTo(sauron.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gameLogContains("is tempted by the Ring")).isTrue();
    }

    @Test
    @DisplayName("The Ring-bearer can still be sacrificed")
    void ringBearerCanBeSacrificed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        Permanent bearer = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.ringBearerId(player1.getId())).isEqualTo(bearer.getId());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DiabolicEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature you do not own")
    void cannotTargetUnownedCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you own");
    }

    @Test
    @CardUsed({SlipOnTheRing.class, GrizzlyBears.class, LayClaim.class})
    void returnsOwnedCreatureControlledByOpponent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID originalId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LayClaim()));
        harness.addMana(player2, ManaColor.BLUE, 7);
        harness.castEnchantment(player2, 0, originalId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, originalId);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Lay Claim");
        assertThat(gd.ringStates.get(player1.getId()).bearerId()).isEqualTo(returned.getId());
    }

    @Test
    void doesNotTemptWhenOnlyTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);

        harness.passBothPriorities();

        assertThat(gd.ringStates.get(player1.getId())).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Slip On the Ring");
    }

    @Test
    void canChooseAnotherCreatureAsRingBearer() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, other.getId());

        assertThat(gd.ringStates.get(player1.getId()).level()).isEqualTo(1);
        assertThat(gd.ringStates.get(player1.getId()).bearerId()).isEqualTo(other.getId());
        assertThat(gqs.hasEffectiveSupertype(gd, other, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Nested
    @DisplayName("The Ring's abilities as it tempts you more times (CR 701.54c)")
    class RingLevels {

        private Permanent fodder;
        private Permanent bearer;

        /** The Ring tempts player1 {@code times} times, each time choosing {@link #bearer} as Ring-bearer. */
        private void temptTimes(int times) {
            fodder = addCreatureReady(player1, new GrizzlyBears());
            bearer = addCreatureReady(player1, new GrizzlyBears());
            for (int i = 0; i < times; i++) {
                harness.setHand(player1, List.of(new SlipOnTheRing()));
                harness.addMana(player1, ManaColor.WHITE, 2);
                harness.castAndResolveInstant(player1, 0, fodder.getId());
                harness.handlePermanentChosen(player1, bearer.getId());
                fodder = gd.playerBattlefields.get(player1.getId()).stream()
                        .filter(permanent -> permanent != bearer).findFirst().orElseThrow();
            }
            assertThat(gd.ringLevel(player1.getId())).isEqualTo(times);
            assertThat(gd.ringBearerId(player1.getId())).isEqualTo(bearer.getId());
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        }

        private int indexOf(Permanent permanent) {
            return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
        }

        private void attackWith(Permanent attacker) {
            harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(indexOf(attacker))));
        }

        /** Resolves the level-2 loot of an attack: draws the top Forest, then discards it. */
        private void resolveLoot() {
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player1, 0);
            resolveAllTriggers();
        }

        private void leaveEndOfCombat() {
            harness.forceStep(TurnStep.COMBAT_DAMAGE);
            harness.clearPriorityPassed();
            harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        }

        @Test
        @DisplayName("Tempted once, the Ring-bearer attacking triggers nothing")
        void levelOneHasNoAttackAbility() {
            temptTimes(1);

            attackWith(bearer);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Tempted twice, whenever your Ring-bearer attacks, draw a card, then discard a card")
        void levelTwoLootsWhenRingBearerAttacks() {
            temptTimes(2);

            attackWith(bearer);
            assertThat(gd.stack).hasSize(1);
            resolveLoot();

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            harness.assertInGraveyard(player1, "Forest");
        }

        @Test
        @DisplayName("Tempted twice, a creature that isn't your Ring-bearer attacking doesn't loot")
        void levelTwoIgnoresOtherAttackers() {
            temptTimes(2);
            fodder.setSummoningSick(false);

            attackWith(fodder);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Tempted three times, each creature blocking your Ring-bearer is sacrificed by its controller at end of combat")
        void levelThreeSacrificesBlockersAtEndOfCombat() {
            temptTimes(3);
            addCreatureReady(player2, new WallOfEarth());
            addCreatureReady(player2, new WallOfEarth());

            attackWith(bearer);
            resolveLoot();
            prepareDeclareBlockers();
            int attackerIndex = indexOf(bearer);
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex),
                    new BlockerAssignment(1, attackerIndex)));
            resolveAllTriggers();
            harness.assertOnBattlefield(player2, "Wall of Earth");

            leaveEndOfCombat();
            resolveAllTriggers();

            harness.assertNotOnBattlefield(player2, "Wall of Earth");
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .filteredOn(card -> card.getName().equals("Wall of Earth")).hasSize(2);
        }

        @Test
        @DisplayName("Tempted twice, a creature blocking your Ring-bearer stays")
        void levelTwoDoesNotSacrificeBlockers() {
            temptTimes(2);
            addCreatureReady(player2, new WallOfEarth());

            attackWith(bearer);
            resolveLoot();
            prepareDeclareBlockers();
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, indexOf(bearer))));
            resolveAllTriggers();
            leaveEndOfCombat();
            resolveAllTriggers();

            harness.assertOnBattlefield(player2, "Wall of Earth");
        }

        @Test
        @DisplayName("Tempted four times, your Ring-bearer dealing combat damage to a player makes each opponent lose 3 life")
        void levelFourDrainsOnCombatDamage() {
            temptTimes(4);

            attackWith(bearer);
            resolveLoot();
            resolveCombat();
            resolveAllTriggers();

            harness.assertLife(player2, 15);
        }

        @Test
        @DisplayName("Tempted three times, your Ring-bearer's combat damage costs no extra life")
        void levelThreeHasNoCombatDamageAbility() {
            temptTimes(3);

            attackWith(bearer);
            resolveLoot();
            resolveCombat();
            resolveAllTriggers();

            harness.assertLife(player2, 18);
        }

        @Test
        @DisplayName("With a Ring each, only the attacking player's Ring loots for their Ring-bearer")
        void eachPlayersRingWatchesOnlyTheirOwnRingBearer() {
            harness.forceActivePlayer(player2);
            Permanent opponentsBearer = addCreatureReady(player2, new GrizzlyBears());
            for (int i = 0; i < 2; i++) {
                harness.setHand(player2, List.of(new SlipOnTheRing()));
                harness.addMana(player2, ManaColor.WHITE, 2);
                harness.castAndResolveInstant(player2, 0,
                        gd.playerBattlefields.get(player2.getId()).getFirst().getId());
            }
            opponentsBearer = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(gd.ringLevel(player2.getId())).isEqualTo(2);
            assertThat(gd.ringBearerId(player2.getId())).isEqualTo(opponentsBearer.getId());
            harness.setHand(player2, List.of());
            harness.forceActivePlayer(player1);
            temptTimes(2);

            attackWith(bearer);

            assertThat(gd.stack).singleElement()
                    .satisfies(entry -> assertThat(entry.getControllerId()).isEqualTo(player1.getId()));
            resolveLoot();
            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
            harness.assertInGraveyard(player1, "Forest");
        }

        @Test
        @DisplayName("The Ring is an emblem its player gets the first time it tempts them, and only once")
        void ringIsOneEmblem() {
            temptTimes(2);

            assertThat(gd.emblems).filteredOn(emblem -> emblem.controllerId().equals(player1.getId()))
                    .singleElement()
                    .satisfies(emblem -> assertThat(emblem.sourceCard().getName()).isEqualTo("The Ring"));
        }
    }
}
