package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.cards.g.Geistflame;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HollowhengeScavenger.class, TyphoidRats.class, Geistflame.class, BrimstoneVolley.class})
class HollowhengeScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not gain life without morbid")
    void noLifeGainWithoutMorbid() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HollowhengeScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // No morbid — no ETB trigger should fire, life stays at 20
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gains 5 life when morbid is met")
    void gainsLifeWithMorbid() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HollowhengeScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Simulate a creature having died this turn
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB trigger goes on stack)
        harness.passBothPriorities(); // resolve ETB (gain 5 life)

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Killing a creature with Geistflame enables morbid life gain")
    void actualCreatureDeathEnablesMorbid() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Geistflame(), new HollowhengeScavenger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addToBattlefield(player2, new TyphoidRats());

        // Kill Typhoid Rats with Geistflame
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Typhoid Rats"));

        // Now cast Hollowhenge Scavenger — morbid should be active
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB (gain 5 life)

        harness.assertLife(player1, 25);

        // Verify it's on the battlefield
        harness.assertOnBattlefield(player1, "Hollowhenge Scavenger");
    }

    @Test
    @DisplayName("A death after Scavenger enters cannot enable its missed trigger")
    void deathAfterEntryDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HollowhengeScavenger(), new Geistflame()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player2, new TyphoidRats());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Typhoid Rats"));

        harness.assertInGraveyard(player2, "Typhoid Rats");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature dying while Scavenger is on the stack enables morbid")
    void deathBeforeSpellResolvesEnablesMorbid() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HollowhengeScavenger(), new Geistflame()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player1, new TyphoidRats());

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Typhoid Rats"));
        harness.assertInGraveyard(player1, "Typhoid Rats");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A creature card already in a graveyard does not enable morbid")
    void graveyardCreatureWithoutDeathDoesNotEnableMorbid() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player2, List.of(new TyphoidRats()));
        harness.setHand(player1, List.of(new HollowhengeScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Scavenger does not stop its life gain trigger")
    void lifeGainResolvesAfterSourceDies() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Geistflame(), new HollowhengeScavenger(), new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addToBattlefield(player2, new TyphoidRats());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Typhoid Rats"));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Hollowhenge Scavenger"));
        harness.assertInGraveyard(player1, "Hollowhenge Scavenger");
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        assertThat(gd.stack).isEmpty();
    }
}
