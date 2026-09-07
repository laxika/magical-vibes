package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpelTheInterlopers.class, AirElemental.class, GrizzlyBears.class, HillGiant.class,
        JayemdaeTome.class})
class ExpelTheInterlopersTest extends BaseCardTest {

    @Test
    @DisplayName("Prompts for a number from 0 through 10")
    void promptsForNumberFromZeroThroughTen() {
        castExpelTheInterlopers();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly(
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        harness.handleListChoice(player1, "10");
    }

    @Test
    @DisplayName("Destroys creatures whose current power meets the chosen threshold")
    void destroysCreaturesAtLeastChosenPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player1, new JayemdaeTome());

        castExpelTheInterlopers();
        harness.handleListChoice(player1, "3");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Jayemdae Tome");
    }

    @Test
    @DisplayName("Choosing zero destroys all creatures")
    void choosingZeroDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castExpelTheInterlopers();
        harness.handleListChoice(player1, "0");

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    private void castExpelTheInterlopers() {
        harness.setHand(player1, List.of(new ExpelTheInterlopers()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
