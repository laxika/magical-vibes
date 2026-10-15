package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZombieInfestation.class, RuneclawBear.class, Mountain.class, Forest.class, Naturalize.class})
class ZombieInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding two cards creates a 2/2 black Zombie token")
    void discardTwoCreatesZombie() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new RuneclawBear(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Discarding two cards leaves the rest of the hand untouched")
    void discardingTwoLeavesRemainingCardsInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ZombieInfestation());
        RuneclawBear remainingCard = new RuneclawBear();
        harness.setHand(player1, List.of(remainingCard, new Mountain(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remainingCard);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate with fewer than two cards in hand")
    void cannotActivateWithOneCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new RuneclawBear()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("The discard cost is paid before the token ability resolves")
    void discardIsPaidBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new Mountain(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Zombie");

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated twice before either activation resolves")
    void canActivateRepeatedlyWithoutManaOrTapping() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent infestation = harness.addToBattlefieldAndReturn(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new Mountain(), new Forest(), new Mountain(), new Forest()));

        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, 0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(infestation.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Zombie");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
    }

    @Test
    @DisplayName("The controller can activate during the opponent's turn")
    void canActivateOnOpponentsTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player2, new ZombieInfestation());
        harness.setHand(player2, List.of(new Mountain(), new Forest()));

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Zombie")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Zombie");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Destroying the enchantment in response does not stop its ability")
    void abilityResolvesAfterSourceIsDestroyed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent infestation = harness.addToBattlefieldAndReturn(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new Mountain(), new Forest()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.castInstant(player2, 0, infestation.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Zombie Infestation");
        harness.assertInGraveyard(player1, "Zombie Infestation");
        harness.assertNotOnBattlefield(player1, "Zombie");

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Zombie");
    }
}
