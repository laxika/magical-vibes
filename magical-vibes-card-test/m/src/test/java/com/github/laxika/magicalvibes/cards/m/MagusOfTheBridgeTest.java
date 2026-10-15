package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoblinAnarchomancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheBridge.class, GoblinAnarchomancer.class, MishrasFactory.class})
class MagusOfTheBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature entering your graveyard creates a 2/2 black Zombie")
    void ownNontokenCreatureDeathCreatesZombie() {
        harness.addToBattlefield(player1, new MagusOfTheBridge());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GoblinAnarchomancer());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GoblinAnarchomancer());

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

    @Test
    void simultaneousOwnCreatureDeathsCreateZombieForEachCreature() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheBridge());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinAnarchomancer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .performSimultaneousRemovals(gd, List.of(magus, goblin), () -> {
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, magus);
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, goblin);
                }));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    @Test
    void stolenMagusDyingIntoOpponentGraveyardDoesNotCreateZombie() {
        MagusOfTheBridge card = new MagusOfTheBridge();
        card.setOwnerId(player2.getId());
        Permanent magus = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(magus.getId(), player2.getId());

        putIntoGraveyard(magus);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.assertInGraveyard(player2, "Magus of the Bridge");
    }

    @Test
    void opponentTokenDeathExilesMagus() {
        MagusOfTheBridge card = new MagusOfTheBridge();
        harness.addToBattlefield(player1, card);
        Permanent token = harness.addToBattlefieldAndReturn(player2, creatureToken());

        putIntoGraveyard(token);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void creatureOwnedByControllerDyingUnderOpponentControlCreatesZombie() {
        harness.addToBattlefield(player1, new MagusOfTheBridge());
        GoblinAnarchomancer card = new GoblinAnarchomancer();
        card.setOwnerId(player1.getId());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(goblin.getId(), player1.getId());

        putIntoGraveyard(goblin);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        harness.assertOnBattlefield(player1, "Magus of the Bridge");
    }

    @Test
    void animatedLandDeathCreatesZombie() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new MishrasFactory());
        harness.addToBattlefield(player1, new MagusOfTheBridge());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        putIntoGraveyard(factory);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
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
