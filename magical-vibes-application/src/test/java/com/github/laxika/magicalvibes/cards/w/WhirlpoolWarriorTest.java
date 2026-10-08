package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.n.NotionThief;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhirlpoolWarrior.class, NotionThief.class})
class WhirlpoolWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability wheels only its controller's hand")
    void entersWheelsOnlyItsControllersHand() {
        harness.setHand(player1, List.of(new WhirlpoolWarrior(), new WhirlpoolWarrior(), new WhirlpoolWarrior()));
        harness.setHand(player2, List.of(new WhirlpoolWarrior()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.setLibrary(player2, libraryWithThreeCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gameLogContains(player1.getUsername() + " shuffles 2 cards from hand into their library.")).isTrue();
        assertThat(gameLogContains(player1.getUsername() + " draws 2 cards.")).isTrue();
        assertThat(gameLogContains(player2.getUsername() + " shuffles")).isFalse();
        assertThat(gameLogContains(player2.getUsername() + " draws")).isFalse();
    }

    @Test
    @DisplayName("Its activated ability sacrifices it and wheels each player's hand")
    void activatedAbilitySacrificesAndWheelsEachPlayersHand() {
        harness.setHand(player1, List.of(new WhirlpoolWarrior(), new WhirlpoolWarrior()));
        harness.setHand(player2, List.of(new WhirlpoolWarrior()));
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
        assertThat(gameLogContains(player1.getUsername() + " shuffles 2 cards from hand into their library.")).isTrue();
        assertThat(gameLogContains(player1.getUsername() + " draws 2 cards.")).isTrue();
        assertThat(gameLogContains(player2.getUsername() + " shuffles 1 card from hand into their library.")).isTrue();
        assertThat(gameLogContains(player2.getUsername() + " draws 1 card.")).isTrue();
    }

    @Test
    @DisplayName("An empty hand draws no cards when the creature enters")
    void entersWithEmptyHand() {
        harness.setHand(player1, List.of(new WhirlpoolWarrior()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Whirlpool Warrior");
    }

    @Test
    @DisplayName("The activated ability handles an empty controller hand and an empty opposing library")
    void activatedAbilityHandlesEmptyZones() {
        harness.setHand(player1, List.of());
        Card opposingHandCard = new WhirlpoolWarrior();
        harness.setHand(player2, List.of(opposingHandCard));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new WhirlpoolWarrior());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Whirlpool Warrior");
        harness.assertInGraveyard(player1, "Whirlpool Warrior");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingHandCard);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingHandCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The activated ability uses the hand sizes at resolution")
    void activatedAbilityCountsHandsAtResolution() {
        harness.setHand(player1, List.of(new WhirlpoolWarrior()));
        harness.setHand(player2, List.of(new WhirlpoolWarrior()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.setLibrary(player2, libraryWithThreeCards());
        harness.addToBattlefield(player1, new WhirlpoolWarrior());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new WhirlpoolWarrior(), new WhirlpoolWarrior()));
        harness.setHand(player2, List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }
    @Test
    @DisplayName("Each player shuffles before any draws can be redirected into another hand")
    void allHandsAreShuffledBeforeDrawing() {
        harness.setHand(player1, List.of(new WhirlpoolWarrior(), new WhirlpoolWarrior()));
        harness.setHand(player2, List.of(new WhirlpoolWarrior()));
        harness.setLibrary(player1, libraryWithThreeCards());
        harness.setLibrary(player2, libraryWithThreeCards());
        harness.addToBattlefield(player1, new WhirlpoolWarrior());
        harness.addToBattlefield(player2, new NotionThief());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gameLogContains(player2.getUsername()
                + " shuffles 1 card from hand into their library.")).isTrue();
        assertThat(gameLogContains(player2.getUsername() + " draws 1 card.")).isTrue();
    }
    private List<Card> libraryWithThreeCards() {
        return List.of(new WhirlpoolWarrior(), new WhirlpoolWarrior(), new WhirlpoolWarrior());
    }
}
