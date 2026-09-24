package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaiseThePalisade.class, AvianChangeling.class, GoblinPiker.class, GrizzlyBears.class})
class RaiseThePalisadeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creatures that are not of the chosen type")
    void returnsCreaturesExceptChosenType() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoblinPiker());
        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BEAR");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Goblin Piker");
    }

    @Test
    @DisplayName("A changeling counts as the chosen creature type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player2, new AvianChangeling());
        harness.addToBattlefield(player2, new GoblinPiker());
        cast();

        harness.handleListChoice(player1, "BIRD");

        harness.assertOnBattlefield(player2, "Avian Changeling");
        harness.assertInHand(player2, "Goblin Piker");
    }

    private void cast() {
        harness.setHand(player1, List.of(new RaiseThePalisade()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
