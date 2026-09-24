package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GaleriderSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavabellySliver.class, GaleriderSliver.class, GrizzlyBears.class})
class LavabellySliverTest extends BaseCardTest {

    @Test
    @DisplayName("Lavabelly Sliver's own entry deals damage and gains life")
    void ownEntryDealsDamageAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        castCreature(player1, new LavabellySliver(), ManaColor.RED, ManaColor.WHITE);

        chooseTargetAndResolve(player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Another Sliver you control gets Lavabelly Sliver's entry trigger")
    void grantsTriggerToAnotherSliverYouControl() {
        harness.addToBattlefield(player1, new LavabellySliver());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        castCreature(player1, new GaleriderSliver(), ManaColor.BLUE);
        chooseTargetAndResolve(player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Lavabelly Sliver does not grant the trigger to non-Slivers or an opponent's Slivers")
    void onlyOwnSliversGetTheTrigger() {
        harness.addToBattlefield(player1, new LavabellySliver());
        harness.addToBattlefield(player2, new GaleriderSliver());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        castCreature(player1, new GrizzlyBears(), ManaColor.GREEN);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.forceActivePlayer(player2);
        castCreature(player2, new GaleriderSliver(), ManaColor.BLUE);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void castCreature(com.github.laxika.magicalvibes.model.Player player, Card card, ManaColor... colors) {
        harness.setHand(player, List.of(card));
        for (ManaColor color : colors) {
            harness.addMana(player, color, 1);
        }
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
    }

    private void chooseTargetAndResolve(java.util.UUID targetId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }
}
