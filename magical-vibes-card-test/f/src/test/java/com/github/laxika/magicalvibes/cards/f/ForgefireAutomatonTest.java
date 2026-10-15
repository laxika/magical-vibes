package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgefireAutomaton.class, GrizzlyBears.class})
class ForgefireAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a smaller creature and perpetually sets its base power to Forgefire Automaton's power")
    void returnsSmallerCreatureAndSetsItsBasePower() {
        ForgefireAutomaton automaton = new ForgefireAutomaton();
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, automaton);
        harness.setGraveyard(player1, List.of(bears));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCard().getPower()).isEqualTo(8);
        assertThat(returned.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(8);
    }

    @Test
    @DisplayName("Does not target a creature whose power equals Forgefire Automaton's power")
    void doesNotTargetEqualPowerCreature() {
        harness.addToBattlefield(player1, new ForgefireAutomaton());
        harness.setGraveyard(player1, List.of(new ForgefireAutomaton()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Forgefire Automaton");
    }

    @Test
    @DisplayName("Prototype uses three power for the upkeep ability")
    void prototypeUsesPrototypePower() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new ForgefireAutomaton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();
        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCard().getPower()).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
    }
}
