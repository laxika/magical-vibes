package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HearthbornBattler.class, GrizzlyBears.class})
class HearthbornBattlerTest extends BaseCardTest {

    @Test
    void damagesOpponentOnTheirSecondSpellOnly() {
        harness.addToBattlefield(player1, new HearthbornBattler());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 6);
        prepareOpponentTurn();

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.castCreature(player2, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void damagesOpponentWhenControllerCastsTheirSecondSpell() {
        harness.addToBattlefield(player1, new HearthbornBattler());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void cannotTargetController() {
        harness.addToBattlefield(player1, new HearthbornBattler());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        prepareOpponentTurn();

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.castCreature(player2, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
