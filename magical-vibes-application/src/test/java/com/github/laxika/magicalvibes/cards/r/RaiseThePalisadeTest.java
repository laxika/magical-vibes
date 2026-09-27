package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
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

@CardUsed({RaiseThePalisade.class, GoblinPiker.class, GrizzlyBears.class,
        GloriousAnthem.class, AvianChangeling.class})
class RaiseThePalisadeTest extends BaseCardTest {

    @Test
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castRaiseThePalisade();
        harness.handleListChoice(player1, "GOBLIN");

        harness.assertOnBattlefield(player1, "Avian Changeling");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving Raise the Palisade awaits a creature type choice")
    void resolvingAwaitsCreatureTypeChoice() {
        harness.addToBattlefield(player2, new GoblinPiker());
        castRaiseThePalisade();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Returns creatures not of the chosen type and leaves matching creatures and noncreatures")
    void returnsCreaturesNotOfChosenType() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        castRaiseThePalisade();

        harness.handleListChoice(player1, "GOBLIN");

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Goblin Piker");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Raise the Palisade");
    }

    private void castRaiseThePalisade() {
        harness.setHand(player1, List.of(new RaiseThePalisade()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
