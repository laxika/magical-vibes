package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({KessigFlamebreather.class, GrizzlyBears.class, Opt.class, Abrade.class})
class KessigFlamebreatherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell deals 1 damage to each opponent")
    void noncreatureSpellDealsDamage() {
        harness.addToBattlefield(player1, new KessigFlamebreather());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KessigFlamebreather());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not trigger")
    void opponentNoncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KessigFlamebreather());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The cast trigger resolves before the noncreature spell")
    @CardUsed({KessigFlamebreather.class, Abrade.class})
    void triggerResolvesBeforeSpell() {
        harness.addToBattlefield(player1, new KessigFlamebreather());
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player1, "Kessig Flamebreather"));
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Kessig Flamebreather");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kessig Flamebreather");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Each Flamebreather triggers independently even if one dies before resolution")
    @CardUsed({KessigFlamebreather.class, Abrade.class})
    void multipleTriggersSurviveSourceRemoval() {
        harness.addToBattlefield(player1, new KessigFlamebreather());
        harness.addToBattlefield(player1, new KessigFlamebreather());
        var sourceId = harness.getPermanentId(player1, "Kessig Flamebreather");
        harness.setHand(player1, List.of(new Abrade()));
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, sourceId);
        harness.castInstant(player2, 0, 0, sourceId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kessig Flamebreather");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Abrade");
    }
}
