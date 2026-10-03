package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrogskolReaver.class, AngelOfMercy.class, GrizzlyBears.class, SoulWarden.class, TurnToFrog.class})
class DrogskolReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when controller gains life")
    void drawsCardOnLifeGain() {
        harness.addToBattlefield(player1, new DrogskolReaver());

        // Cast Angel of Mercy (ETB: gain 3 life) to trigger life gain
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB triggers)
        harness.passBothPriorities(); // resolve life gain triggered ability (GainLifeEffect)
        harness.passBothPriorities(); // resolve Reaver's draw triggered ability

        // Hand: setHand(1) -> cast Angel(0) -> draw 1 = 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when opponent gains life")
    void noDrawWhenOpponentGainsLife() {
        harness.addToBattlefield(player1, new DrogskolReaver());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB triggers)
        harness.passBothPriorities(); // resolve life gain triggered ability

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Each life gain event triggers an independent draw")
    void multipleLifeGainEventsEachDraw() {
        harness.addToBattlefield(player1, new DrogskolReaver());
        harness.addToBattlefield(player1, new SoulWarden());

        // Cast a creature — Soul Warden triggers (gain 1 life), which triggers the Reaver
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (Soul Warden triggers)
        harness.passBothPriorities(); // resolve Soul Warden's GainLifeEffect
        harness.passBothPriorities(); // resolve Reaver's draw

        // setHand(1) -> cast Grizzly(0) -> draw 1 = 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Separate creature entries cause separate life gains and draws")
    void separateLifeGainsDrawTwice() {
        harness.addToBattlefield(player1, new DrogskolReaver());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 21);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Double strike lifelink draws once in each damage step")
    void doubleStrikeLifelinkDrawsTwice() {
        Permanent reaver = harness.addToBattlefieldAndReturn(player1, new DrogskolReaver());
        reaver.setSummoningSick(false);
        reaver.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Each Reaver draws once for a single three-life gain")
    void multipleReaversEachDrawOnce() {
        harness.addToBattlefield(player1, new DrogskolReaver());
        harness.addToBattlefield(player1, new DrogskolReaver());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Life gain does not trigger a Reaver that has lost all abilities")
    void noDrawAfterLosingAbilities() {
        Permanent reaver = harness.addToBattlefieldAndReturn(player1, new DrogskolReaver());
        harness.setHand(player1, List.of(new TurnToFrog(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, reaver.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
