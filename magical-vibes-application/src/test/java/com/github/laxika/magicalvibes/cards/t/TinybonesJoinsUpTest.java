package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TinybonesJoinsUp.class, Forest.class, GrizzlyBears.class, TymaretTheMurderKing.class})
class TinybonesJoinsUpTest extends BaseCardTest {

    @Test
    void canChooseNoPlayers() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new TinybonesJoinsUp(), "{B}");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void legendaryCreatureCanMakeBothPlayersMillAndLoseLife() {
        harness.addToBattlefield(player1, new TinybonesJoinsUp());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player1, new TymaretTheMurderKing(), "{B}{R}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void entersAndMakesAChosenPlayerDiscard() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        harness.castFromHand(player1, new TinybonesJoinsUp(), "{B}");

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void legendaryCreatureMakesAChosenPlayerMillAndLoseLife() {
        harness.addToBattlefield(player1, new TinybonesJoinsUp());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.setHand(player1, List.of(new TymaretTheMurderKing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }
}
