package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DementiaSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class DementiaSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A matching revealed card is discarded")
    void matchingRevealedCardIsDiscarded() {
        addReadyDementiaSliver(player1);
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player2, List.of(card));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("A nonmatching revealed card remains in its owner's hand")
    void nonmatchingRevealedCardIsNotDiscarded() {
        addReadyDementiaSliver(player1);
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player2, List.of(card));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dementia Sliver");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability is granted to another Sliver")
    void grantsAbilityToAnotherSliver() {
        harness.addToBattlefield(player1, new DementiaSliver());
        addReadySliver(player1);
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player2, List.of(card));

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("The ability is granted to an opposing Sliver")
    void grantsAbilityToOpposingSliver() {
        harness.addToBattlefield(player1, new DementiaSliver());
        addReadySliver(player2);
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Grizzly Bears");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("The ability can target only an opponent and can activate only during its controller's turn")
    void enforcesTargetAndTimingRestrictions() {
        addReadyDementiaSliver(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("The ability is not granted to non-Sliver creatures")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new DementiaSliver());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyDementiaSliver(Player player) {
        addReadySliver(player, new DementiaSliver());
    }

    private void addReadySliver(Player player) {
        addReadySliver(player, new BonescytheSliver());
    }

    private void addReadySliver(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
