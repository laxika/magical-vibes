package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosGuildgate.class})
class RakdosGuildgateTest extends BaseCardTest {

    @Test
    void playedLandEntersTappedWithoutUsingTheStack() {
        harness.setHand(player1, List.of(new RakdosGuildgate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rakdos Guildgate").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersTappedWhenPutOntoTheBattlefield() {
        Permanent gate = harness.enterBattlefieldAndReturn(player1, new RakdosGuildgate());

        assertThat(gate.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "RED"})
    void producesExactlyOneManaOfTheChosenColor(ManaColor color) {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        gate.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "RED");
        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, color.name());

        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedLandCannotProduceMana() {
        harness.enterBattlefieldAndReturn(player1, new RakdosGuildgate());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canProduceManaAfterUntapping() {
        Permanent gate = harness.enterBattlefieldAndReturn(player1, new RakdosGuildgate());

        harness.performUntapStep(player1);
        assertThat(gate.isTapped()).isFalse();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
