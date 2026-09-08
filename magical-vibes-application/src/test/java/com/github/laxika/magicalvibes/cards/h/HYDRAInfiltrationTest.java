package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HYDRAInfiltration.class, GrizzlyBears.class})
class HYDRAInfiltrationTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, target opponent discards two cards")
    void etbMakesTargetOpponentDiscardTwoCards() {
        harness.setHand(player1, List.of(new HYDRAInfiltration()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A creature attacking alone drains the chosen opponent")
    void loneAttackerDrainsTargetOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new HYDRAInfiltration());
        addCreatureReady(player1);

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The attack trigger cannot target its controller")
    void attackTriggerCannotTargetController() {
        harness.addToBattlefield(player1, new HYDRAInfiltration());
        addCreatureReady(player1);

        declareAttackers(List.of(1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking with more than one creature does not trigger the drain")
    void multipleAttackersDoNotTrigger() {
        harness.addToBattlefield(player1, new HYDRAInfiltration());
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1, 2));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
