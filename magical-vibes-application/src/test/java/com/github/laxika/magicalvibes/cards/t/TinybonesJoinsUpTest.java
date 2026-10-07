package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArmoredArmadillo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TinybonesJoinsUp.class, Forest.class, ArmoredArmadillo.class, TinybonesThePickpocket.class})
class TinybonesJoinsUpTest extends BaseCardTest {

    @Test
    void canChooseNoPlayers() {
        harness.setHand(player2, List.of(new ArmoredArmadillo()));
        harness.castFromHand(player1, new TinybonesJoinsUp(), "{B}");
        harness.setHand(player1, List.of(new ArmoredArmadillo()));
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
        harness.castFromHand(player1, new TinybonesThePickpocket(), "{B}");
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
        ArmoredArmadillo discarded = new ArmoredArmadillo();
        harness.setHand(player2, List.of(discarded));
        harness.castFromHand(player1, new TinybonesJoinsUp(), "{B}");

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Armored Armadillo");
    }

    @Test
    void legendaryCreatureMakesAChosenPlayerMillAndLoseLife() {
        harness.addToBattlefield(player1, new TinybonesJoinsUp());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new ArmoredArmadillo(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.castFromHand(player1, new TinybonesThePickpocket(), "{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void bothChosenPlayersDiscardTheirOwnCard() {
        harness.setHand(player2, List.of(new ArmoredArmadillo()));
        harness.castFromHand(player1, new TinybonesJoinsUp(), "{B}");
        harness.setHand(player1, List.of(new Forest()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Armored Armadillo");
    }

    @Test
    void chosenPlayerWithEmptyHandDoesNotPreventOtherPlayerDiscarding() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new TinybonesJoinsUp(), "{B}");
        harness.setHand(player1, List.of(new Forest()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void legendaryCreatureTriggerCanChooseNoPlayers() {
        harness.addToBattlefield(player1, new TinybonesJoinsUp());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player1, new TinybonesThePickpocket(), "{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void chosenPlayerLosesLifeEvenWithEmptyLibrary() {
        harness.addToBattlefield(player1, new TinybonesJoinsUp());
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, new TinybonesThePickpocket(), "{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void opponentsLegendaryCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new TinybonesJoinsUp());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player2, new TinybonesThePickpocket(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
