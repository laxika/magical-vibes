package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SlyInstigator.class, GrizzlyBears.class, Forest.class})
class SlyInstigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Makes an opponent's creature unblockable and goads it until your next turn")
    void makesOpponentCreatureUnblockableAndGoadsIt() {
        Permanent instigator = addCreatureReady(player1, new SlyInstigator());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addBlueMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(instigator.isTapped()).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();

        beginAttackers(player2);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Unblockability lasts through the opponent's turn and expires on your next turn")
    void unblockabilityExpiresOnControllerNextTurn() {
        addCreatureReady(player1, new SlyInstigator());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addBlueMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();

        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, target)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature an opponent controls")
    void cannotTargetOwnCreatureOrOpponentLand() {
        addCreatureReady(player1, new SlyInstigator());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentLand = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(opponentLand);
        addBlueMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlueMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void beginAttackers(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
