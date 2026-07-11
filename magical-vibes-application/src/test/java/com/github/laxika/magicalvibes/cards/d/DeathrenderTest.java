package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathrenderTest extends BaseCardTest {

    // ===== Static — "Equipped creature gets +2/+2" =====

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent deathrender = new Permanent(new Deathrender());
        gd.playerBattlefields.get(player1.getId()).add(deathrender);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        deathrender.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    // ===== Death trigger — put a creature from hand and attach Deathrender to it =====

    @Test
    @DisplayName("When equipped creature dies, chosen creature enters with Deathrender attached")
    void deathTriggerPutsCreatureAndAttaches() {
        Permanent deathrender = new Permanent(new Deathrender());
        gd.playerBattlefields.get(player1.getId()).add(deathrender);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        deathrender.setAttachedTo(bears.getId());

        // Kill the equipped creature with Doom Blade; Air Elemental remains in hand to put.
        harness.setHand(player1, List.of(new DoomBlade(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Doom Blade resolves, bears dies, trigger goes on stack
        harness.passBothPriorities(); // trigger resolves, begins the hand-card choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, 0); // put Air Elemental

        Permanent airElemental = findPermanent(player1, "Air Elemental");
        assertThat(airElemental).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        Permanent deathrenderPerm = findPermanent(player1, "Deathrender");
        assertThat(deathrenderPerm.getAttachedTo()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("Declining the death trigger leaves the creature in hand and Deathrender unattached")
    void deathTriggerDeclined() {
        Permanent deathrender = new Permanent(new Deathrender());
        gd.playerBattlefields.get(player1.getId()).add(deathrender);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        deathrender.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new DoomBlade(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Doom Blade resolves, bears dies, trigger goes on stack
        harness.passBothPriorities(); // trigger resolves, begins the hand-card choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, -1); // decline

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"));
        Permanent deathrenderPerm = findPermanent(player1, "Deathrender");
        assertThat(deathrenderPerm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Death trigger with no creature in hand does nothing")
    void deathTriggerNoCreatureInHand() {
        Permanent deathrender = new Permanent(new Deathrender());
        gd.playerBattlefields.get(player1.getId()).add(deathrender);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        deathrender.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Doom Blade resolves, bears dies, trigger goes on stack
        harness.passBothPriorities(); // trigger resolves with no creature in hand — no choice

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        Permanent deathrenderPerm = findPermanent(player1, "Deathrender");
        assertThat(deathrenderPerm.getAttachedTo()).isNull();
    }
}
