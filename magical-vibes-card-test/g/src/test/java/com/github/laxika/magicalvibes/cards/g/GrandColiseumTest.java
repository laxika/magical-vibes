package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrandColiseum.class})
class GrandColiseumTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new GrandColiseum()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Grand Coliseum").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for colorless adds {C} without dealing damage")
    void tapForColorlessMana() {
        Permanent land = addReadyLand();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for any color adds the chosen mana and deals 1 damage")
    void tapForAnyColorMana() {
        Permanent land = addReadyLand();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canProduceEachColor(ManaColor color) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GrandColiseum());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(land.isTapped()).isTrue();
        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
        }
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateEitherAbilityWhileTapped() {
        harness.setHand(player1, List.of(new GrandColiseum()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersTappedWhenPutOntoBattlefieldWithoutBeingPlayed() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new GrandColiseum());

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void opponentActivationAddsManaAndDealsDamageOnlyToOpponent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GrandColiseum());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.handleListChoice(player2, "GREEN");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GrandColiseum());
        land.setSummoningSick(false);
        return land;
    }
}
