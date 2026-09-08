package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ApprenticeWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireAndBrimstone.class, ApprenticeWizard.class})
class FireAndBrimstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a player who attacked this turn and 4 damage to its controller")
    void damagesAttackingPlayerAndController() {
        addCreatureReady(player2, new ApprenticeWizard());
        declareAttackers(player2, List.of(0));
        harness.setHand(player1, List.of(new FireAndBrimstone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot target a player who did not attack this turn")
    void rejectsPlayerWhoDidNotAttack() {
        addCreatureReady(player2, new ApprenticeWizard());
        declareAttackers(player2, List.of(0));
        harness.setHand(player1, List.of(new FireAndBrimstone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Does nothing if the target no longer attacked this turn when it resolves")
    void rechecksAttackRestrictionAtResolution() {
        addCreatureReady(player2, new ApprenticeWizard());
        declareAttackers(player2, List.of(0));
        harness.setHand(player1, List.of(new FireAndBrimstone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, player2.getId());
        gd.playersDeclaredAttackersThisTurn.clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals both damage portions when you are the targeted attacking player")
    void damagesControllerTwiceWhenTargetingSelf() {
        addCreatureReady(player1, new ApprenticeWizard());
        declareAttackers(player1, List.of(0));
        harness.setHand(player1, List.of(new FireAndBrimstone()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
