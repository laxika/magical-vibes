package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindlashSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class MindlashSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Mindlash Sliver's ability sacrifices itself and makes each player discard")
    void sacrificesItselfAndEachPlayerDiscards() {
        Permanent mindlash = addCreatureReady(player1, new MindlashSliver());
        GrizzlyBears player1Card = new GrizzlyBears();
        GrizzlyBears player2Card = new GrizzlyBears();
        harness.setHand(player1, List.of(player1Card));
        harness.setHand(player2, List.of(player2Card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        discardFor(player1);
        discardFor(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mindlash);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mindlash.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Card);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Card);
    }

    @Test
    @DisplayName("Mindlash Sliver grants the ability to another Sliver")
    void grantsAbilityToAnotherSliver() {
        harness.addToBattlefield(player1, new MindlashSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        discardFor(player1);
        discardFor(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherSliver);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mindlash Sliver grants the ability to an opposing Sliver")
    void grantsAbilityToOpposingSliver() {
        harness.addToBattlefield(player1, new MindlashSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        discardFor(player2);
        discardFor(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingSliver);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mindlash Sliver does not grant the ability to non-Sliver creatures")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new MindlashSliver());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void discardFor(com.github.laxika.magicalvibes.model.Player player) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player.getId());
        harness.handleCardChosen(player, 0);
    }
}
