package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaeveInsidiousSinger.class, GrizzlyBears.class, Island.class})
class MaeveInsidiousSingerTest extends BaseCardTest {

    @Test
    @DisplayName("Goads the target creature until the controller's next turn")
    void goadsTargetCreatureUntilNextTurn() {
        addMaeveAndBears();
        activateMaeve(bears());

        assertThat(als.getMustAttackRequirementCount(gd, bears())).isEqualTo(1);
        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Draws when the watched creature attacks an opponent")
    void drawsWhenWatchedCreatureAttacksOpponent() {
        addMaeveAndBears();
        harness.setLibrary(player1, List.of(new Island()));
        activateMaeve(bears());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Requires a creature target")
    void requiresCreatureTarget() {
        Permanent maeve = addCreatureReady(player1, new MaeveInsidiousSinger());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(maeve), null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addMaeveAndBears() {
        addCreatureReady(player1, new MaeveInsidiousSinger());
        addCreatureReady(player1, new GrizzlyBears());
    }

    private Permanent bears() {
        return findPermanent(player1, "Grizzly Bears");
    }

    private void activateMaeve(Permanent target) {
        addMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
