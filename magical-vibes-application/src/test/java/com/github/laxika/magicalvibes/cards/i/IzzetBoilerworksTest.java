package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IzzetBoilerworks.class, Island.class})
class IzzetBoilerworksTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns a chosen land to its owner's hand")
    void entersTappedAndReturnsChosenLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new IzzetBoilerworks()));

        harness.playLand(player1, 0);

        Permanent boilerworks = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof IzzetBoilerworks)
                .findFirst().orElseThrow();
        assertThat(boilerworks.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, island.getId());

        harness.assertOnBattlefield(player1, "Izzet Boilerworks");
        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Can return itself when it is the only land")
    void canReturnItself() {
        harness.setHand(player1, List.of(new IzzetBoilerworks()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent boilerworks = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(boilerworks.getId());
        harness.handlePermanentChosen(player1, boilerworks.getId());

        harness.assertNotOnBattlefield(player1, "Izzet Boilerworks");
        harness.assertInHand(player1, "Izzet Boilerworks");
    }

    @Test
    @DisplayName("Tapping adds one blue and one red mana")
    void manaAbilityAddsBlueAndRed() {
        Permanent boilerworks = harness.addToBattlefieldAndReturn(player1, new IzzetBoilerworks());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(boilerworks.isTapped()).isTrue();
    }
}
