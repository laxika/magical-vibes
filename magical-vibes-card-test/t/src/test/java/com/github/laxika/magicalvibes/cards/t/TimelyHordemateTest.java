package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JeskaiStudent;
import com.github.laxika.magicalvibes.cards.d.DefiantStrike;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimelyHordemate.class, JeskaiStudent.class, DefiantStrike.class})
class TimelyHordemateTest extends BaseCardTest {

    private void castTimelyHordemate() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TimelyHordemate(), "{3}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Raid returns a chosen creature with mana value 2 or less")
    void raidReturnsCheapCreature() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        JeskaiStudent cheapCreature = new JeskaiStudent();
        TimelyHordemate expensiveCreature = new TimelyHordemate();
        harness.setGraveyard(player1, List.of(expensiveCreature, cheapCreature));

        castTimelyHordemate();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(cheapCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jeskai Student");
        harness.assertInGraveyard(player1, "Timely Hordemate");
        harness.assertNotInGraveyard(player1, "Jeskai Student");
    }

    @Test
    @DisplayName("Raid does not trigger if no creature attacked this turn")
    void noRaidDoesNotTrigger() {
        JeskaiStudent cheapCreature = new JeskaiStudent();
        harness.setGraveyard(player1, List.of(cheapCreature));

        castTimelyHordemate();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cheapCreature);
    }

    @Test
    @DisplayName("Raid ignores creature cards with mana value greater than 2")
    void ignoresExpensiveCreature() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        TimelyHordemate expensiveCreature = new TimelyHordemate();
        harness.setGraveyard(player1, List.of(expensiveCreature));

        castTimelyHordemate();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expensiveCreature);
    }

    @Test
    void ignoresNoncreatureCardsWithLowManaValue() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        DefiantStrike noncreature = new DefiantStrike();
        harness.setGraveyard(player1, List.of(noncreature));

        castTimelyHordemate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
    }

    @Test
    void opponentsAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        JeskaiStudent creature = new JeskaiStudent();
        harness.setGraveyard(player1, List.of(creature));

        castTimelyHordemate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void cannotReturnCreatureFromOpponentsGraveyard() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        JeskaiStudent creature = new JeskaiStudent();
        harness.setGraveyard(player2, List.of(creature));

        castTimelyHordemate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Jeskai Student");
    }

    @Test
    void targetLeavingGraveyardDoesNotReturnAnotherCreature() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        JeskaiStudent target = new JeskaiStudent();
        JeskaiStudent other = new JeskaiStudent();
        harness.setGraveyard(player1, List.of(target, other));

        castTimelyHordemate();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jeskai Student");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.stack).isEmpty();
    }
}
