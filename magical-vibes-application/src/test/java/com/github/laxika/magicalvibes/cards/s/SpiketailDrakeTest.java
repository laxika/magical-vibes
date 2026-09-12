package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Inflame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiketailDrake.class, SpiketailHatchling.class, Inflame.class})
class SpiketailDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice counters a spell when its controller cannot pay {3}")
    void countersSpellWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new SpiketailDrake());

        harness.forceActivePlayer(player2);
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.castFromHand(player2, hatchling, "{1}{U}");
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, hatchling.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiketail Drake");
        harness.assertInGraveyard(player2, "Spiketail Hatchling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The spell survives when its controller pays {3}")
    void spellSurvivesWhenControllerPays() {
        harness.addToBattlefield(player1, new SpiketailDrake());

        harness.forceActivePlayer(player2);
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.castFromHand(player2, hatchling, "{1}{U}");
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, hatchling.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spiketail Hatchling");
        harness.assertInGraveyard(player1, "Spiketail Drake");
    }

    @Test
    @DisplayName("The spell is countered when its controller declines to pay {3}")
    void spellIsCounteredWhenControllerDeclinesToPay() {
        harness.addToBattlefield(player1, new SpiketailDrake());

        harness.forceActivePlayer(player2);
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.castFromHand(player2, hatchling, "{1}{U}");
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, hatchling.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Spiketail Drake");
        harness.assertInGraveyard(player2, "Spiketail Hatchling");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter an own noncreature spell")
    void canCounterOwnNoncreatureSpell() {
        harness.addToBattlefield(player1, new SpiketailDrake());

        harness.forceActivePlayer(player1);
        Inflame inflame = new Inflame();
        harness.castFromHand(player1, inflame, "{R}");
        harness.activateAbility(player1, 0, null, inflame.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiketail Drake");
        harness.assertInGraveyard(player1, "Inflame");
        assertThat(gd.stack).isEmpty();
    }
}
