package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeartPiercerManticoreTest extends BaseCardTest {

    /** Casts Heart-Piercer Manticore and advances to its "sacrifice another creature?" prompt. */
    private void castManticoreToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HeartPiercerManticore()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> enters, ETB may on stack
        harness.passBothPriorities(); // resolve ETB may -> prompt
    }

    @Test
    @DisplayName("Sacrificing a creature deals damage equal to its power to a target player")
    void sacrificeDealsPowerDamageToPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 to sacrifice
        int lifeBefore = gd.getLife(player2.getId());

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());                       // any-target = opponent
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears")); // sacrifice

        // 2 damage (the sacrificed Grizzly Bears' power), not 4 (the Manticore's power).
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Damage equals the sacrificed creature's power, not the Manticore's")
    void damageTracksSacrificedCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());  // 2/2 to sacrifice
        harness.addToBattlefield(player2, new HillGiant());     // 3/3 target

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Hill Giant"));       // target
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));    // sacrifice

        // The 3/3 survives 2 damage; it would have died to the Manticore's own power (4).
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Enough power kills the targeted creature")
    void lethalDamageDestroysTargetCreature() {
        harness.addToBattlefield(player1, new HillGiant());     // 3/3 to sacrifice
        harness.addToBattlefield(player2, new GrizzlyBears());  // 2/2 target

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears")); // target
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Hill Giant"));    // sacrifice

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Declining the sacrifice deals no damage and keeps the creature")
    void decliningSacrificeDealsNoDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("With no other creature to sacrifice, the trigger deals no damage")
    void noOtherCreatureDealsNoDamage() {
        int lifeBefore = gd.getLife(player2.getId());

        castManticoreToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId()); // any-target chosen, but nothing to sacrifice

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }
}
