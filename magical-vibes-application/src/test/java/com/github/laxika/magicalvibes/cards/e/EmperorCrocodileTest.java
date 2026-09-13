package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Compost;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmperorCrocodile.class, MetathranSoldier.class, Eradicate.class,
        Opalescence.class, Compost.class})
class EmperorCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Cast with no other creatures — state trigger fires and Crocodile is sacrificed")
    void sacrificedWhenControllingNoOtherCreatures() {
        harness.setHand(player1, List.of(new EmperorCrocodile()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires

        // State trigger is on the stack — Crocodile still alive
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.assertOnBattlefield(player1, "Emperor Crocodile");

        // Resolve state trigger → Crocodile is sacrificed
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Emperor Crocodile");
        harness.assertInGraveyard(player1, "Emperor Crocodile");
    }

    @Test
    @DisplayName("Survives while controlling another creature — no state trigger")
    void survivesWithAnotherCreature() {
        harness.addToBattlefield(player1, new MetathranSoldier());
        harness.setHand(player1, List.of(new EmperorCrocodile()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Emperor Crocodile");
    }

    @Test
    @DisplayName("Sacrificed when the last other creature dies")
    void sacrificedWhenLastOtherCreatureDies() {
        harness.addToBattlefield(player1, new MetathranSoldier());
        harness.setHand(player1, List.of(new EmperorCrocodile()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Both creatures present, no trigger yet
        assertThat(gd.stack).isEmpty();

        // Exile the Soldier with Eradicate.
        UUID soldierId = harness.getPermanentId(player1, "Metathran Soldier");
        harness.setHand(player2, List.of(new Eradicate()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, soldierId);

        // Soldier gone → state trigger fires; resolve it → Crocodile sacrificed
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Emperor Crocodile");
        harness.assertInGraveyard(player1, "Emperor Crocodile");
    }

    @Test
    @DisplayName("Controlling another creature owned via opponent does not count — sacrificed")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player2, new MetathranSoldier());
        harness.setHand(player1, List.of(new EmperorCrocodile()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // state trigger fires — opponent's creature doesn't count
        harness.passBothPriorities(); // resolve → sacrificed

        harness.assertNotOnBattlefield(player1, "Emperor Crocodile");
        harness.assertInGraveyard(player1, "Emperor Crocodile");
    }

    @Test
    @DisplayName("Survives while another noncreature permanent is effectively a creature")
    void survivesWithEffectivelyCreaturePermanent() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player1, new EmperorCrocodile());

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Compost"))).isTrue();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Emperor Crocodile");
    }
}
