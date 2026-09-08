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

        harness.castSorcery(player1, 0, List.of(
                stolen.getId(), supportedFirst.getId(), supportedSecond.getId()));
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, List.of(stolen.getId(), supported.getId()));
        harness.passBothPriorities();

        assertThat(supported.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("May support no creatures")
    void maySupportNoCreatures() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        harness.castSorcery(player1, 0, List.of(stolen.getId()));
        harness.passBothPriorities();

        assertThat(stolen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("May support the creature it gains control of")
    void maySupportTheControlledCreature() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PressIntoService()));
        addMana();

        harness.castSorcery(player1, 0, List.of(stolen.getId(), stolen.getId()));
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, List.of(stolen.getId(), supported.getId()));
        harness.passBothPriorities();
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

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
