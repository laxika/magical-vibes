package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.ProwlersHelm;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SatyrPiper.class, TravelingPhilosopher.class, ProwlersHelm.class})
class SatyrPiperTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability makes target creature must be blocked")
    void activatedAbilityMakesTargetMustBeBlocked() {
        addReadyPiper(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Must-be-blocked requirement wears off at end of turn")
    void mustBeBlockedRequirementWearsOff() {
        addReadyPiper(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyPiper(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ProwlersHelm());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One blocker satisfies the requirement even when another can block")
    void requiresOneBlockerNotAllBlockers() {
        addReadyPiper(player1);
        Permanent attacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        Permanent otherBlocker = addReadyCreature(player2);
        addActivationMana();
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(otherBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A tapped defender need not block the affected attacker")
    void permitsNoBlocksWhenNoCreatureCanBlock() {
        addReadyPiper(player1);
        Permanent attacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.tap();
        addActivationMana();
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Piper can activate while tapped and summoning sick")
    void activationDoesNotRequireTappingOrHaste() {
        Permanent piper = harness.addToBattlefieldAndReturn(player1, new SatyrPiper());
        piper.setSummoningSick(true);
        piper.tap();
        addActivationMana();

        harness.activateAbility(player1, 0, null, piper.getId());
        harness.passBothPriorities();

        assertThat(piper.isMustBeBlockedThisTurn()).isTrue();
        assertThat(piper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two affected attackers require blockers to be split when possible")
    void cannotDoubleBlockOneAffectedAttackerAndIgnoreTheOther() {
        addReadyPiper(player1);
        Permanent first = addReadyCreature(player1);
        Permanent second = addReadyCreature(player1);
        addReadyCreature(player2);
        addReadyCreature(player2);
        addActivationMana();
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        addActivationMana();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(1, 2));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1), new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A lone blocker may choose either of two affected attackers")
    void insufficientBlockersAllowChoosingWhichRequirementToSatisfy() {
        addReadyPiper(player1);
        Permanent first = addReadyCreature(player1);
        Permanent second = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        addActivationMana();
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        addActivationMana();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(1, 2));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 2)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyPiper(Player player) {
        return addReadyPermanent(player, new SatyrPiper());
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyPermanent(player, new TravelingPhilosopher());
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
