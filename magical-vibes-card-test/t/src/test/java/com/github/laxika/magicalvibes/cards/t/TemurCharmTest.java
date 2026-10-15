package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemurCharm.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class, Unsummon.class})
class TemurCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 boosts your creature before it fights an opposing creature")
    void boostsAndFights() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMode(0, List.of(own.getId(), opposing.getId()));

        assertThat(own.getEffectivePower()).isEqualTo(3);
        assertThat(own.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 1 counters a spell whose controller cannot pay {3}")
    void countersSpellWithoutPayment() {
        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.castFromHand(player2, elves, "{G}");

        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);

        harness.passPriority(player2);
        harness.castInstant(player1, 0, 1, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Mode 1 cannot target a permanent")
    void counterModeRejectsPermanentTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 prevents creatures with power 3 or less from blocking")
    void preventsLowPowerBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent lowPowerBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent highPowerBlocker = addCreatureReady(player2, new AirElemental());

        castMode(2, List.of());

        assertThat(bls.canBlockAttacker(gd, lowPowerBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, highPowerBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsOwnCreatureWhenOpponentTargetLeaves() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);
        harness.castModalInstant(player1, 0, 0, List.of(own.getId(), opposing.getId()));
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, opposing.getId());
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(3);
        assertThat(own.getEffectiveToughness()).isEqualTo(3);
        assertThat(own.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void doesNotBoostOrDamageOpponentWhenOwnTargetLeaves() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);
        harness.castModalInstant(player1, 0, 0, List.of(own.getId(), opposing.getId()));
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, own.getId());
        harness.passBothPriorities();

        assertThat(opposing.getEffectivePower()).isEqualTo(2);
        assertThat(opposing.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposing.getMarkedDamage()).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void fightModeRejectsReversedControllers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0,
                List.of(opposing.getId(), own.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fightModeRejectsSecondCreatureYouControl() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0,
                List.of(own.getId(), other.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spellResolvesWhenControllerPaysThree() {
        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.castFromHand(player2, elves, "{G}");
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);
        castMode(1, List.of(elves.getId()));

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void countersWhenControllerDeclinesPayment() {
        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.castFromHand(player2, elves, "{G}");
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);
        castMode(1, List.of(elves.getId()));

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    void blockingRestrictionUsesCurrentPowerIncludingThree() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        castMode(2, List.of());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        blocker.setPowerModifier(-1);
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        blocker.setPowerModifier(0);
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void blockingRestrictionAppliesToCreaturesEnteringLaterAndBothPlayers() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        Permanent opposingAttacker = addCreatureReady(player2, new GrizzlyBears());
        castMode(2, List.of());
        Permanent lateBlocker = addCreatureReady(player2, new AirElemental());
        lateBlocker.setPowerModifier(-1);
        Permanent ownBlocker = addCreatureReady(player1, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, lateBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, ownBlocker, opposingAttacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    void blockingRestrictionExpiresAfterTurnEnds() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        castMode(2, List.of());
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    private void castMode(int modeIndex, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);
        harness.castModalInstant(player1, 0, modeIndex, targetIds);
        harness.passBothPriorities();
    }

    private void addTemurMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.RED, 1);
    }
}
