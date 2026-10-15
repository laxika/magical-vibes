package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.j.JadeGuardian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempestCaller.class, GrizzlyBears.class, JayemdaeTome.class, JadeGuardian.class})
class TempestCallerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack targeting opponent")
    void resolvingPutsEtbOnStack() {
        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Tempest Caller");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB taps all creatures target opponent controls")
    void etbTapsAllOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(p -> !p.isTapped());

        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(battlefield).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Does not tap non-creature permanents of target opponent")
    void doesNotTapNonCreatures() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        Permanent artifact = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(artifact.isTapped()).isFalse();

        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not tap controller's creatures")
    void doesNotTapControllerCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ownCreature = gd.playerBattlefields.get(player1.getId()).getFirst();

        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tempest Caller remains on battlefield after ETB resolves")
    void remainsOnBattlefield() {
        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Tempest Caller");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new TempestCaller()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Resolves without error when opponent has no creatures")
    void worksWithEmptyBattlefield() {
        castTempestCaller();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tempest Caller");
    }

    @Test
    @DisplayName("Hexproof creatures are tapped because only their controller is targeted")
    void tapsHexproofCreatures() {
        harness.addToBattlefield(player2, new JadeGuardian());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();

        castTempestCaller();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves are tapped too")
    void tapsCreaturesPresentAtResolution() {
        castTempestCaller();
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The trigger resolves even if Tempest Caller leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();
        castTempestCaller();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castTempestCaller() {
        harness.setHand(player1, List.of(new TempestCaller()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, player2.getId());
    }
}
