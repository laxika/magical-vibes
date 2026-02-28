package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SylvokLifestaffTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sylvok Lifestaff has static +1/+0 boost effect")
    void hasStaticBoostEffect() {
        SylvokLifestaff card = new SylvokLifestaff();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostAttachedCreatureEffect.class);
        BoostAttachedCreatureEffect boost = (BoostAttachedCreatureEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Sylvok Lifestaff has equipped creature death trigger")
    void hasEquippedCreatureDeathTrigger() {
        SylvokLifestaff card = new SylvokLifestaff();

        assertThat(card.getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES).getFirst())
                .isInstanceOf(GainLifeEffect.class);
        GainLifeEffect gainLife = (GainLifeEffect) card.getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES).getFirst();
        assertThat(gainLife.amount()).isEqualTo(3);
    }

    // ===== Static boost =====

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent lifestaff = addLifestaffReady(player1);
        lifestaff.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unequipped creature does not get boost")
    void unequippedCreatureNoBoost() {
        Permanent creature = addReadyCreature(player1);
        addLifestaffReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("Controller gains 3 life when equipped creature dies")
    void gainsLifeWhenEquippedCreatureDies() {
        Permanent creature = addReadyCreature(player1);
        Permanent lifestaff = addLifestaffReady(player1);
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
        Permanent creature = addReadyCreature(player1);
        addLifestaffReady(player1); // not attached

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
        Permanent creature = addReadyCreature(player1);
        Permanent lifestaff = addLifestaffReady(player1);
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Equipment should still be on the battlefield, unattached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sylvok Lifestaff"));
        assertThat(lifestaff.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Trigger does not fire for a different creature dying")
    void triggerDoesNotFireForDifferentCreature() {
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent lifestaff = addLifestaffReady(player1);
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

    // ===== Helpers =====

    private Permanent addLifestaffReady(Player player) {
        Permanent perm = new Permanent(new SylvokLifestaff());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
