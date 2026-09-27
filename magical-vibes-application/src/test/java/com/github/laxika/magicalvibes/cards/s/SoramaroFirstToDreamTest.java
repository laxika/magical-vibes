package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoramaroFirstToDream.class, OboroPalaceInTheClouds.class})
class SoramaroFirstToDreamTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of cards in its controller's hand")
    void powerAndToughnessEqualHandSize() {
        Permanent permanent = addSoramaro();

        harness.setHand(player1, List.of(new OboroPalaceInTheClouds(), new OboroPalaceInTheClouds()));
        harness.setHand(player2, List.of(new OboroPalaceInTheClouds(), new OboroPalaceInTheClouds(),
                new OboroPalaceInTheClouds(), new OboroPalaceInTheClouds()));
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);

        harness.setHand(player1, List.of(new OboroPalaceInTheClouds(), new OboroPalaceInTheClouds(),
                new OboroPalaceInTheClouds(), new OboroPalaceInTheClouds()));
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(4);
    }

    @Test
    @DisplayName("Returns a land as a cost and draws a card")
    void returnsLandAndDrawsCard() {
        Permanent soramaro = addSoramaro();
        OboroPalaceInTheClouds returnedLand = new OboroPalaceInTheClouds();
        OboroPalaceInTheClouds drawnCard = new OboroPalaceInTheClouds();
        harness.addToBattlefield(player1, returnedLand);
        harness.setHand(player1, List.of(new OboroPalaceInTheClouds()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(soramaro), 0, null);

        assertThat(gd.playerHands.get(player1.getId())).contains(returnedLand);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(drawnCard);
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        Permanent soramaro = addSoramaro();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(soramaro), 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate using a land controlled by an opponent")
    void cannotActivateUsingOpponentsLand() {
        Permanent soramaro = addSoramaro();
        harness.addToBattlefield(player2, new OboroPalaceInTheClouds());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(soramaro), 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent soramaro = addSoramaro();
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(soramaro), 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSoramaro() {
        return addCreatureReady(player1, new SoramaroFirstToDream());
    }
}
