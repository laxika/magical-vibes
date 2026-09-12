package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rethink.class, SpiketailHatchling.class})
class RethinkTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay its mana value")
    void countersWhenControllerCannotPayManaValue() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.setHand(player1, List.of(hatchling));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Rethink()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hatchling.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiketail Hatchling");
        harness.assertNotOnBattlefield(player1, "Spiketail Hatchling");
    }

    @Test
    @DisplayName("Allows a spell to resolve when its controller pays its mana value")
    void payingManaValueKeepsSpellOnStack() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.setHand(player1, List.of(hatchling));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setHand(player2, List.of(new Rethink()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hatchling.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player1, "Spiketail Hatchling");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spiketail Hatchling");
    }

    @Test
    @DisplayName("Counters a spell when its controller declines to pay its mana value")
    void countersWhenControllerDeclinesManaValuePayment() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.setHand(player1, List.of(hatchling));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setHand(player2, List.of(new Rethink()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hatchling.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Spiketail Hatchling");
        harness.assertNotOnBattlefield(player1, "Spiketail Hatchling");
    }
}
