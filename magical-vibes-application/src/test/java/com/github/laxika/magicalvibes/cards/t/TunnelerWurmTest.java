package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TunnelerWurm.class, GrizzlyBears.class})
class TunnelerWurmTest extends BaseCardTest {

    @Test
    void discardingACardGrantsARegenerationShield() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new TunnelerWurm());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(wurm.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        harness.addToBattlefieldAndReturn(player1, new TunnelerWurm());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
