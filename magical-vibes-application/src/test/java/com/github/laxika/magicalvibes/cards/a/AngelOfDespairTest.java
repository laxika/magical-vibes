package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EarthSurge;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.g.GruulTurf;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfDespair.class, EarthSurge.class, GruulSignet.class, GruulTurf.class})
class AngelOfDespairTest extends BaseCardTest {

    @Test
    void entersAndDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new GruulSignet());
        castAngel(harness.getPermanentId(player2, "Gruul Signet"));

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Angel of Despair");
        harness.assertNotOnBattlefield(player2, "Gruul Signet");
        harness.assertInGraveyard(player2, "Gruul Signet");
    }

    @Test
    void entersAndDestroysTargetLand() {
        harness.addToBattlefield(player2, new GruulTurf());
        castAngel(harness.getPermanentId(player2, "Gruul Turf"));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Gruul Turf");
        harness.assertInGraveyard(player2, "Gruul Turf");
    }

    @Test
    void canDestroyOwnTargetPermanent() {
        harness.addToBattlefield(player1, new EarthSurge());
        castAngel(harness.getPermanentId(player1, "Earth Surge"));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Earth Surge");
        harness.assertInGraveyard(player1, "Earth Surge");
    }

    @Test
    void etbFizzlesIfTargetIsRemovedBeforeResolution() {
        harness.addToBattlefield(player2, new GruulSignet());
        castAngel(harness.getPermanentId(player2, "Gruul Signet"));

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertOnBattlefield(player1, "Angel of Despair");
    }

    @Test
    void mustTargetItselfWhenEnteringAnEmptyBattlefield() {
        harness.castFromHand(player1, new AngelOfDespair(), "{3}{W}{W}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Angel of Despair"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Angel of Despair");
        harness.assertInGraveyard(player1, "Angel of Despair");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersAndDestroysTargetCreature() {
        harness.addToBattlefield(player2, new AngelOfDespair());
        castAngel(harness.getPermanentId(player2, "Angel of Despair"));

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Angel of Despair");
        harness.assertNotOnBattlefield(player2, "Angel of Despair");
        harness.assertInGraveyard(player2, "Angel of Despair");
    }

    @Test
    void canTargetItselfEvenWhenAnotherPermanentIsAvailable() {
        harness.addToBattlefield(player2, new GruulSignet());
        harness.castFromHand(player1, new AngelOfDespair(), "{3}{W}{W}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Angel of Despair"));

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Angel of Despair");
        harness.assertNotOnBattlefield(player1, "Angel of Despair");
        harness.assertOnBattlefield(player2, "Gruul Signet");
    }

    @Test
    void triggersWhenEnteringWithoutBeingCast() {
        harness.addToBattlefield(player2, new GruulSignet());
        harness.enterBattlefieldAndReturn(player1, new AngelOfDespair());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Gruul Signet"));

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Angel of Despair");
        harness.assertNotOnBattlefield(player2, "Gruul Signet");
        harness.assertInGraveyard(player2, "Gruul Signet");
    }

    @Test
    void destructionTriggerResolvesAfterAngelLeavesBattlefield() {
        harness.addToBattlefield(player2, new GruulSignet());
        castAngel(harness.getPermanentId(player2, "Gruul Signet"));
        gd.playerBattlefields.get(player1.getId()).clear();

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Gruul Signet");
        harness.assertInGraveyard(player2, "Gruul Signet");
    }

    private void castAngel(UUID targetId) {
        harness.castFromHand(player1, new AngelOfDespair(), "{3}{W}{W}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
    }
}
