package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AbsorbVis;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreadRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and makes target opponent lose 3 life")
    void exilesCreatureAndOpponentLosesLife() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new DreadRider());
        rider.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int riderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rider);
        harness.activateAbility(player1, riderIndex, null, player2.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(rider.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new DreadRider());
        rider.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int riderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rider);
        assertThatThrownBy(() -> harness.activateAbility(player1, riderIndex, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new DreadRider());
        rider.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new AbsorbVis()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int riderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rider);
        assertThatThrownBy(() -> harness.activateAbility(player1, riderIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
