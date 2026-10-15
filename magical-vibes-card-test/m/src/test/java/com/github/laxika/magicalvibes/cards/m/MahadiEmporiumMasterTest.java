package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MahadiEmporiumMaster.class, GrizzlyBears.class, Shock.class})
class MahadiEmporiumMasterTest extends BaseCardTest {

    @Test
    void createsTreasureForEachCreatureThatDiedThisTurn() {
        harness.addToBattlefield(player1, new MahadiEmporiumMaster());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, opposingCreature.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void doesNotCreateTreasureWhenNoCreatureDiedThisTurn() {
        harness.addToBattlefield(player1, new MahadiEmporiumMaster());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void countsCreaturesThatDiedBeforeMahadiEntered() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.addToBattlefield(player1, new MahadiEmporiumMaster());
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new MahadiEmporiumMaster());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void countsDeathInResponseEvenWhenNoCreatureHadDiedAtTriggerTime() {
        harness.addToBattlefield(player1, new MahadiEmporiumMaster());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void triggerResolvesAndCountsMahadiWhenMahadiDiesInResponse() {
        Permanent mahadi = harness.addToBattlefieldAndReturn(player1, new MahadiEmporiumMaster());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, mahadi.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, mahadi.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Mahadi, Emporium Master");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
