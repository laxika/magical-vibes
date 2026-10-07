package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvokLifestaff.class, GrizzlyBears.class, Deathmark.class})
class SylvokLifestaffTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lifestaff = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        lifestaff.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unequipped creature does not get boost")
    void unequippedCreatureNoBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller gains 3 life when equipped creature dies")
    void gainsLifeWhenEquippedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lifestaff = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        lifestaff.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Opponent destroys the equipped creature with Deathmark
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Deathmark — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve GainLifeEffect trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("No life gained when unequipped creature dies")
    void noLifeWhenUnequippedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff()); // not attached

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Opponent destroys the creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Deathmark

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Equipment stays on battlefield unattached after equipped creature dies")
    void equipmentStaysOnBattlefieldAfterCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lifestaff = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        lifestaff.setAttachedTo(creature.getId());

        // Opponent destroys the equipped creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Deathmark
        harness.passBothPriorities(); // resolve trigger

        // Creature should be in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Equipment should still be on the battlefield, unattached
        harness.assertOnBattlefield(player1, "Sylvok Lifestaff");
        assertThat(lifestaff.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Trigger does not fire for a different creature dying")
    void triggerDoesNotFireForDifferentCreature() {
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent lifestaff = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        lifestaff.setAttachedTo(creature1.getId());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Opponent destroys the NON-equipped creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature2.getId());
        harness.passBothPriorities(); // resolve Deathmark

        // Life should not have changed — no trigger should have fired
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        // Equipment should still be attached to creature1
        assertThat(lifestaff.getAttachedTo()).isEqualTo(creature1.getId());
    }

    @Test
    @DisplayName("Equip costs one mana and moves the power bonus")
    void equipMovesPowerBonus() {
        Permanent lifestaff = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, first.getId());
        assertThat(lifestaff.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(lifestaff.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        assertThat(lifestaff.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipment controller gains life when an opposing equipped creature dies")
    void equipmentControllerGainsLifeForOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent lifestaff = harness.addToBattlefieldAndReturn(player1, new SylvokLifestaff());
        lifestaff.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        resolveAllTriggers();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
