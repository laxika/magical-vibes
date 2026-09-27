package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheBridge.class, GrizzlyBears.class})
class MagusOfTheBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature entering your graveyard creates a 2/2 black Zombie")
    void ownNontokenCreatureDeathCreatesZombie() {
        harness.addToBattlefield(player1, new MagusOfTheBridge());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        putIntoGraveyard(bears);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        Permanent zombie = findPermanents(player1, "Zombie").getFirst();
        assertThat(zombie.getEffectivePower()).isEqualTo(2);
        assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
    }

    @Test
    @DisplayName("Magus of the Bridge creates a Zombie when it dies")
    void ownDeathCreatesZombie() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheBridge());

        putIntoGraveyard(magus);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        harness.assertInGraveyard(player1, "Magus of the Bridge");
    }

    @Test
    @DisplayName("An opponent's creature entering their graveyard exiles Magus")
    void opponentCreatureDeathExilesMagus() {
        MagusOfTheBridge magusCard = new MagusOfTheBridge();
        harness.addToBattlefield(player1, magusCard);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        putIntoGraveyard(bears);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(magusCard);
        harness.assertNotOnBattlefield(player1, "Magus of the Bridge");
    }

    @Test
    @DisplayName("A token creature entering your graveyard does not create a Zombie")
    void tokenCreatureDeathDoesNotCreateZombie() {
        harness.addToBattlefield(player1, new MagusOfTheBridge());
        Permanent token = harness.addToBattlefieldAndReturn(player1, creatureToken());

        putIntoGraveyard(token);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
    }

    private Card creatureToken() {
        Card token = new Card();
        token.setName("Bear Token");
        token.setToken(true);
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.GREEN);
        token.setPower(2);
        token.setToughness(2);
        return token;
    }
}
