package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhirlpoolWarrior.class, GrizzlyBears.class})
class WhirlpoolWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability wheels only its controller's hand")
    void entersWheelsOnlyItsControllersHand() {
        harness.setHand(player1, List.of(new WhirlpoolWarrior(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.setLibrary(player2, libraryWithThreeCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Its activated ability sacrifices it and wheels each player's hand")
    void activatedAbilitySacrificesAndWheelsEachPlayersHand() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.setLibrary(player2, libraryWithThreeCards());

        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new WhirlpoolWarrior());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(warrior);
        harness.assertInGraveyard(player1, "Whirlpool Warrior");
    }

    private List<Card> libraryWithThreeCards() {
        return List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
