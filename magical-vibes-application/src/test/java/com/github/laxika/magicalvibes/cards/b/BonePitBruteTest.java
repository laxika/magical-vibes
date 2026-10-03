package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BonePitBrute.class, GrizzlyBears.class, FountainOfYouth.class})
class BonePitBruteTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +4/+0")
    void etbBoostsTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BonePitBrute()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getPowerModifier()).isEqualTo(4);
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getEffectivePower()).isEqualTo(6);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BonePitBrute()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if target creature leaves before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BonePitBrute()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new BonePitBrute()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enter an empty battlefield and target itself")
    void canTargetItselfAfterEntering() {
        harness.setHand(player1, List.of(new BonePitBrute()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent brute = findPermanent(player1, "Bone Pit Brute");
        harness.handlePermanentChosen(player1, brute.getId());
        resolveAllTriggers();

        assertThat(brute.getEffectivePower()).isEqualTo(8);
        assertThat(brute.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("ETB still resolves after Bone Pit Brute leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BonePitBrute());
        harness.setHand(player1, List.of(new BonePitBrute()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent != target);
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Menace prevents a single creature from blocking")
    void cannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new BonePitBrute());
        addCreatureReady(player2, new BonePitBrute());
        addCreatureReady(player2, new BonePitBrute());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace permits two creatures to block")
    void canBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new BonePitBrute());
        Permanent first = addCreatureReady(player2, new BonePitBrute());
        Permanent second = addCreatureReady(player2, new BonePitBrute());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
