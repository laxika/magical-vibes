package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnluckyWitness.class, Forest.class, GrizzlyBears.class, Shock.class})
class UnluckyWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it exiles two cards and lets you choose one to play until your next end step")
    void deathTriggerExilesTwoAndGrantsChosenCardPermission() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent witness = addCreatureReady(player1, new UnluckyWitness());
        Card chosen = new Forest();
        Card other = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, other));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, witness.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen, other);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(other.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosen.getId());
    }
}
