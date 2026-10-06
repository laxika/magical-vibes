package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkirsdagSupplicant.class, GrizzlyBears.class})
class SkirsdagSupplicantTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card makes each player lose 2 life and taps Skirsdag Supplicant")
    void eachPlayerLosesLife() {
        Permanent supplicant = addCreatureReady(player1, new SkirsdagSupplicant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL);
        harness.setLife(player2, GameData.STARTING_LIFE_TOTAL);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(supplicant.isTapped()).isTrue();
        harness.assertLife(player1, GameData.STARTING_LIFE_TOTAL - 2);
        harness.assertLife(player2, GameData.STARTING_LIFE_TOTAL - 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent supplicant = addCreatureReady(player1, new SkirsdagSupplicant());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(supplicant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Discard and tap are costs; life loss waits for resolution")
    void costsArePaidBeforeLifeLoss() {
        Permanent supplicant = addCreatureReady(player1, new SkirsdagSupplicant());
        harness.setHand(player1, List.of(new SkirsdagSupplicant(), new SkirsdagSupplicant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 15);
        harness.setLife(player2, 17);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);

        assertThat(supplicant.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Skirsdag Supplicant");
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 17);

        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void cannotActivateWithoutBlackMana() {
        Permanent supplicant = addCreatureReady(player1, new SkirsdagSupplicant());
        harness.setHand(player1, List.of(new SkirsdagSupplicant()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(supplicant.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent supplicant = addCreatureReady(player1, new SkirsdagSupplicant());
        supplicant.tap();
        harness.setHand(player1, List.of(new SkirsdagSupplicant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent supplicant = addCreatureReady(player1, new SkirsdagSupplicant());
        supplicant.setSummoningSick(true);
        harness.setHand(player1, List.of(new SkirsdagSupplicant()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(supplicant.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
