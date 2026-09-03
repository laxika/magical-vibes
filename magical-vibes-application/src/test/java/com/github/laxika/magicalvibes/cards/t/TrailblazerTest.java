package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Trailblazer.class, BalduvianBears.class, Forest.class})
class TrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving makes target creature unblockable this turn")
    void resolvingMakesCreatureUnblockable() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new Trailblazer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Trailblazer");
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.setHand(player1, List.of(new Trailblazer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableResetsAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new Trailblazer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The target cannot be declared as blocked this turn")
    void targetCannotBeBlockedThisTurn() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());

        harness.setHand(player1, List.of(new Trailblazer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new Trailblazer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
