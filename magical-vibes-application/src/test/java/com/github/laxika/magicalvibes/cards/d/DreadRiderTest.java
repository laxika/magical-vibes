package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AbsorbVis;
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

@CardUsed({DreadRider.class, GrizzlyBears.class, AbsorbVis.class})
class DreadRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and makes target opponent lose 3 life")
    void exilesCreatureAndOpponentLosesLife() {
        Permanent rider = addCreatureReady(player1, new DreadRider());
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
        Permanent rider = addCreatureReady(player1, new DreadRider());
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
        Permanent rider = addCreatureReady(player1, new DreadRider());
        harness.setGraveyard(player1, List.of(new AbsorbVis()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int riderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rider);
        assertThatThrownBy(() -> harness.activateAbility(player1, riderIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        Permanent rider = addCreatureReady(player1, new DreadRider());
        DreadRider fuel = new DreadRider();
        harness.setGraveyard(player1, List.of(fuel));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        assertThat(rider.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fuel);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new DreadRider());
        harness.setGraveyard(player1, List.of(new DreadRider()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent rider = addCreatureReady(player1, new DreadRider());
        rider.setTapped(true);
        harness.setGraveyard(player1, List.of(new DreadRider()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotUseOpponentsGraveyardToPayCost() {
        addCreatureReady(player1, new DreadRider());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new DreadRider()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        harness.assertInGraveyard(player2, "Dread Rider");
    }

    @Test
    void costsArePaidBeforeResolutionAndAbilitySurvivesSourceLeaving() {
        Permanent rider = addCreatureReady(player1, new DreadRider());
        DreadRider fuel = new DreadRider();
        harness.setGraveyard(player1, List.of(fuel));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(rider.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(fuel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(fuel);
        harness.assertLife(player2, 20);

        gd.playerBattlefields.get(player1.getId()).remove(rider);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(fuel);
    }
}
