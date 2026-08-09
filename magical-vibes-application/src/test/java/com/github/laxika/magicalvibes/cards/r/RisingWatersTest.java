package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RisingWatersTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped lands stay tapped through the untap step while creatures untap normally")
    void landsDontUntap() {
        harness.addToBattlefield(player1, new RisingWaters());
        Permanent land = addTapped(player1, new Forest());
        Permanent bears = addTapped(player1, new GrizzlyBears());

        advanceToNextTurn(player2);

        assertThat(land.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The active player chooses one of their own lands to untap at their upkeep")
    void activePlayerUntapsOwnLand() {
        harness.addToBattlefield(player1, new RisingWaters());
        Permanent landA = addTapped(player1, new Forest());
        Permanent landB = addTapped(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(landA.getId()));

        assertThat(landA.isTapped()).isFalse();
        assertThat(landB.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At an opponent's upkeep only that opponent's lands are offered")
    void opponentChoosesFromTheirOwnLands() {
        harness.addToBattlefield(player1, new RisingWaters());
        Permanent ownLand = addTapped(player1, new Forest());
        Permanent enemyLand = addTapped(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(enemyLand.getId()).doesNotContain(ownLand.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(enemyLand.getId()));

        assertThat(enemyLand.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isTrue();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
