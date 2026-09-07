package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinMachinist.class, Forest.class, Shock.class})
class GoblinMachinistTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals until a nonland, boosts by its mana value, and lets its controller order the cards")
    void revealsUntilNonlandBoostsAndOrdersCards() {
        Permanent machinist = addCreatureReady(player1, new GoblinMachinist());
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(firstLand, secondLand, shock));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, machinist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, machinist)).isEqualTo(5);
        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(firstLand, secondLand, shock);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondLand, firstLand, shock);
    }

    @Test
    @DisplayName("Does not boost when the library contains no nonland card")
    void noNonlandCardDoesNotBoost() {
        Permanent machinist = addCreatureReady(player1, new GoblinMachinist());
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gqs.getEffectivePower(gd, machinist)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, machinist)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondLand, firstLand);
    }

    @Test
    @DisplayName("The power boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent machinist = addCreatureReady(player1, new GoblinMachinist());
        harness.setLibrary(player1, List.of(new Shock()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, machinist)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, machinist)).isZero();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
