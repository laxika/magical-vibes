package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpenTheGraves.class, GrizzlyBears.class, WrathOfGod.class, Card.class})
class OpenTheGravesTest extends BaseCardTest {

    @Test
    @DisplayName("Your nontoken creature dying creates a 2/2 black Zombie")
    void allyCreatureDeathCreatesZombie() {
        harness.addToBattlefield(player1, new OpenTheGraves());
        harness.addToBattlefield(player1, new GrizzlyBears());

        wrathFromOpponent();

        List<Permanent> zombies = findPermanents(player1, "Zombie");
        assertThat(zombies).hasSize(1);
        Permanent zombie = zombies.getFirst();
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getEffectivePower()).isEqualTo(2);
        assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Two of your creatures dying creates two Zombies")
    void twoDeathsCreateTwoZombies() {
        harness.addToBattlefield(player1, new OpenTheGraves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        wrathFromOpponent();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's creature dying does not create a Zombie")
    void opponentCreatureDeathCreatesNothing() {
        harness.addToBattlefield(player1, new OpenTheGraves());
        harness.addToBattlefield(player2, new GrizzlyBears());

        wrathFromOpponent();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("A token creature dying does not create a Zombie")
    void tokenDeathCreatesNothing() {
        harness.addToBattlefield(player1, new OpenTheGraves());

        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        harness.addToBattlefield(player1, tokenCard);

        wrathFromOpponent();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Multiple copies each trigger for the same creature death")
    void multipleCopiesEachCreateZombie() {
        harness.addToBattlefield(player1, new OpenTheGraves());
        harness.addToBattlefield(player1, new OpenTheGraves());
        harness.addToBattlefield(player1, new GrizzlyBears());

        wrathFromOpponent();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    @Test
    @DisplayName("A Zombie created by the ability does not replace itself when it dies")
    void createdZombieDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new OpenTheGraves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        wrathFromOpponent();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);

        wrathFromOpponent();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each player gets tokens only for their own nontoken creatures")
    void eachControllerGetsTheirOwnZombies() {
        harness.addToBattlefield(player1, new OpenTheGraves());
        harness.addToBattlefield(player2, new OpenTheGraves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        wrathFromOpponent();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanents(player2, "Zombie")).hasSize(2);
    }

    private void wrathFromOpponent() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");

        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
