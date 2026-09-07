package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LandEquilibrium.class, Forest.class, Island.class})
class LandEquilibriumTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent sacrifices a chosen land after putting a land onto the battlefield")
    void opponentSacrificesChosenLandAfterPlayingLand() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player2, List.of(new Island()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).context())
                .isInstanceOf(PermanentChoiceContext.LandEquilibriumSacrifice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId(),
                        gd.playerBattlefields.get(player2.getId()).get(2).getId());

        harness.handlePermanentChosen(player2, firstLand.getId());

        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(secondLand)
                .noneMatch(permanent -> permanent.getId().equals(firstLand.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The replacement does not apply when the opponent has fewer lands before entry")
    void doesNotApplyWhenOpponentHasFewerLands() {
        harness.addToBattlefield(player1, new LandEquilibrium());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new Island()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
