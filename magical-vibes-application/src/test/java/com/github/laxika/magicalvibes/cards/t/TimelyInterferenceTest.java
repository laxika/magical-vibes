package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimelyInterference.class, GrizzlyBears.class, Forest.class})
class TimelyInterferenceTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -1/-0 and draws a card")
    void weakensAndDrawsWithoutKicker() {
        Permanent target = addCreatureReady(player2);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TimelyInterference()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("The kicked spell also requires the target creature to block")
    void kickedSpellRequiresTargetToBlock() {
        Permanent target = addCreatureReady(player2);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TimelyInterference()));
        addKickedMana();

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("A kicked target must be declared as a blocker when able")
    void kickedTargetMustBlockWhenAble() {
        Permanent attacker = addCreatureReady(player1);
        Permanent target = addCreatureReady(player2);
        castKicked(target);

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(target.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The temporary effects expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addCreatureReady(player2);
        castKicked(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TimelyInterference()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castKicked(Permanent target) {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TimelyInterference()));
        addKickedMana();
        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
