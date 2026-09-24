package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogWitch.class, FreshVolunteers.class})
class BogWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Paying black mana and discarding a card adds three black mana")
    void paysManaAndDiscardToAddBlackMana() {
        Permanent bogWitch = addCreatureReady(player1, new BogWitch());
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(6);
        assertThat(bogWitch.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        Permanent bogWitch = addCreatureReady(player1, new BogWitch());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(bogWitch.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without the required black mana")
    void cannotActivateWithoutBlackMana() {
        Permanent bogWitch = addCreatureReady(player1, new BogWitch());
        harness.setHand(player1, List.of(new FreshVolunteers()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(bogWitch.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent bogWitch = harness.addToBattlefieldAndReturn(player1, new BogWitch());
        harness.setHand(player1, List.of(new FreshVolunteers()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(bogWitch.isTapped()).isFalse();
    }
}
