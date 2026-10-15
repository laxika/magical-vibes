package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PressIntoService.class, GrizzlyBears.class, Pacifism.class})
class PressIntoServiceTest extends BaseCardTest {

    @Test
    @DisplayName("Supports up to two creatures and temporarily steals, untaps, and hastes another creature")
    void supportsTwoCreaturesAndStealsAnotherCreature() {
        Permanent supportedFirst = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent supportedSecond = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        stolen.tap();
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(
                stolen.getId(), supportedFirst.getId(), supportedSecond.getId()));

        assertThat(supportedFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(supportedSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(stolen.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(stolen.getId())).isTrue();
    }

    @Test
    @DisplayName("May support only one creature")
    void maySupportOnlyOneCreature() {
        Permanent supported = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(stolen.getId(), supported.getId()));

        assertThat(supported.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("May support no creatures")
    void maySupportNoCreatures() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(stolen.getId()));

        assertThat(stolen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("May support the creature it gains control of")
    void maySupportTheControlledCreature() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(stolen.getId(), stolen.getId()));

        assertThat(stolen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup while counters remain")
    void controlAndHasteExpireAtCleanup() {
        Permanent supported = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(stolen.getId(), supported.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(stolen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(supported.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot use a noncreature as the creature-control target")
    void cannotTargetNoncreatureForControl() {
        Permanent supported = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondSupported = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(
                        enchantment.getId(), supported.getId(), secondSupported.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot support the same creature twice")
    void cannotSupportTheSameCreatureTwice() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent supported = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(stolen.getId(), supported.getId(), supported.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can untap and haste a creature already controlled by the caster")
    void canTargetOwnCreatureForControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Support still resolves when the control target leaves the battlefield")
    void supportsWhenControlTargetLeaves() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent supported = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();
        harness.castSorcery(player1, 0, List.of(stolen.getId(), supported.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, stolen));

        harness.passBothPriorities();

        assertThat(supported.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(supported.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(supported);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(supported);
    }

    @Test
    @DisplayName("Remaining support and control targets resolve when one support target leaves")
    void resolvesRemainingTargetsWhenSupportTargetLeaves() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent supported = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        stolen.tap();
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();
        harness.castSorcery(player1, 0, List.of(stolen.getId(), removed.getId(), supported.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removed));

        harness.passBothPriorities();

        assertThat(supported.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(stolen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(stolen.isTapped()).isFalse();
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolen);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
