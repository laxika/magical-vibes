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
import java.util.Map;

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

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
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
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        addBlueMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlueMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Test
    @DisplayName("A goaded creature can attack the goading player in a two-player game and cannot be blocked")
    void goadedCreatureCanAttackGoadingPlayerButCannotBeBlocked() {
        addCreatureReady(player1, new SlyInstigator());
        addCreatureReady(player1, new SlyInstigator());
        Permanent target = addCreatureReady(player2, new SlyInstigator());
        addBlueMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, Map.of(1, 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both effects remain after Sly Instigator leaves the battlefield")
    void effectsRemainAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new SlyInstigator());
        Permanent target = addCreatureReady(player2, new SlyInstigator());
        addBlueMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();
        assertThat(gqs.isGoaded(gd, target)).isTrue();
    }

    @Test
    @DisplayName("Both effects expire when the ability controller's next turn begins")
    void goadAlsoExpiresOnControllerNextTurn() {
        addCreatureReady(player1, new SlyInstigator());
        Permanent target = addCreatureReady(player2, new SlyInstigator());
        addBlueMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.isGoaded(gd, target)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, target)).isFalse();
        assertThat(gqs.isGoaded(gd, target)).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Sly Instigator cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new SlyInstigator());
        Permanent target = addCreatureReady(player2, new SlyInstigator());
        addBlueMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.hasCantBeBlocked(gd, target)).isFalse();
        assertThat(gqs.isGoaded(gd, target)).isFalse();
    }

    @Test
    @DisplayName("The activation requires one blue mana")
    void activationRequiresBlueMana() {
        Permanent source = addCreatureReady(player1, new SlyInstigator());
        Permanent target = addCreatureReady(player2, new SlyInstigator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(gqs.isGoaded(gd, target)).isFalse();
    }
}
