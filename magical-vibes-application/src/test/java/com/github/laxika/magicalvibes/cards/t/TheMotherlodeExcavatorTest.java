package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BloatflySwarm;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.m.MirelurkQueen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMotherlodeExcavator.class, EvolvingWilds.class, Forest.class,
        MirelurkQueen.class, BloatflySwarm.class})
class TheMotherlodeExcavatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with energy equal to target opponent's nonbasic lands")
    void entersWithEnergyForTargetOpponentsNonbasicLands() {
        harness.addToBattlefield(player2, new EvolvingWilds());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TheMotherlodeExcavator()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays four energy to destroy a nonbasic land and stop nonfliers from blocking")
    void paysEnergyToDestroyLandAndStopNonfliersFromBlocking() {
        Permanent motherlode = addCreatureReady(player1, new TheMotherlodeExcavator());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent groundCreature = addCreatureReady(player2, new MirelurkQueen());
        Permanent flyingCreature = harness.enterBattlefieldAndReturn(player2, new BloatflySwarm());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        resolveAttackToPayment();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonbasicLand);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(nonbasicLand.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handlePermanentChosen(player1, nonbasicLand.getId());
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonbasicLand);
            resolveAllTriggers();
        });

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(nonbasicLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(basicLand, flyingCreature);
        assertThat(bls.canBlockAttacker(gd, groundCreature, motherlode,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingCreature, motherlode,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Declining the payment does nothing")
    void decliningPaymentDoesNothing() {
        addCreatureReady(player1, new TheMotherlodeExcavator());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        Permanent groundCreature = addCreatureReady(player2, new MirelurkQueen());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        resolveAttackToPayment();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonbasicLand);
        assertThat(bls.canBlockAttacker(gd, groundCreature,
                gd.playerBattlefields.get(player1.getId()).getFirst(),
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the attack cost without enough energy")
    void cannotPayWithoutEnoughEnergy() {
        addCreatureReady(player1, new TheMotherlodeExcavator());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        Permanent groundCreature = addCreatureReady(player2, new MirelurkQueen());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
            resolveAllTriggers();
            if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
                harness.handleMayAbilityChosen(player1, true);
            }
        });
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonbasicLand);
        assertThat(bls.canBlockAttacker(gd, groundCreature,
                gd.playerBattlefields.get(player1.getId()).getFirst(),
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void entryCountsNonbasicLandsAtResolutionAndIgnoresControllersLands() {
        harness.addToBattlefield(player1, new EvolvingWilds());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TheMotherlodeExcavator()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new EvolvingWilds());
        harness.addToBattlefield(player2, new EvolvingWilds());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void entryWithNoNonbasicLandsGivesNoEnergy() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TheMotherlodeExcavator()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void mayPayEvenWhenDefenderHasNoNonbasicLand() {
        addCreatureReady(player1, new TheMotherlodeExcavator());
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new MirelurkQueen());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        resolveAttackToPayment();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(bls.canBlockAttacker(gd, blocker,
                gd.playerBattlefields.get(player1.getId()).getFirst(),
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void losingLandTargetAfterPaymentDoesNotRefundEnergyOrPreventBlocking() {
        Permanent motherlode = addCreatureReady(player1, new TheMotherlodeExcavator());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        Permanent blocker = addCreatureReady(player2, new MirelurkQueen());
        harness.setLibrary(player2, List.of());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        resolveAttackToPayment();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, land.getId());
            harness.activateAbility(player2, 0, null, null);
            resolveAllTriggers();
        });

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(bls.canBlockAttacker(gd, blocker, motherlode,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void restrictionAppliesToNonfliersEnteringAfterResolution() {
        Permanent motherlode = addCreatureReady(player1, new TheMotherlodeExcavator());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        gd.playerEnergyCounters.put(player1.getId(), 5);

        resolveAttackToPayment();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, land.getId());
            resolveAllTriggers();
        });
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new MirelurkQueen());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(bls.canBlockAttacker(gd, blocker, motherlode,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @CardUsed({GarrukWildspeaker.class})
    void attackingPlaneswalkerPreventsItsControllersNonfliersFromBlocking() {
        Permanent motherlode = addCreatureReady(player1, new TheMotherlodeExcavator());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new GarrukWildspeaker());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        Permanent blocker = addCreatureReady(player2, new MirelurkQueen());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, land.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(bls.canBlockAttacker(gd, blocker, motherlode,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    private void resolveAttackToPayment() {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
            resolveAllTriggers();
        });
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
