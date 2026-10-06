package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RobeOfStars.class, GrizzlyBears.class, Disenchant.class})
class RobeOfStarsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+3")
    void equippedCreatureGetsToughnessBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent robe = addRobeAttachedTo(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(robe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Astral Projection phases out the equipped creature and Robe of Stars")
    void astralProjectionPhasesOutEquippedCreatureAndEquipment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent robe = addRobeAttachedTo(creature);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, robe);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, robe);
        assertThat(robe.isPhasedOutIndirectly()).isTrue();
    }

    @Test
    @DisplayName("The equipped creature and Robe of Stars phase in on the creature controller's next untap")
    void phasesInOnNextUntap() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent robe = addRobeAttachedTo(creature);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, robe);

        advanceToUpkeep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, robe);
        assertThat(robe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(robe.isPhasedOutIndirectly()).isFalse();
    }

    private Permanent addRobeAttachedTo(Permanent creature) {
        Permanent robe = harness.addToBattlefieldAndReturn(player1, new RobeOfStars());
        robe.setAttachedTo(creature.getId());
        return robe;
    }

    @Test
    void equipAttachesAndTransfersToughnessBoost() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent robe = harness.addToBattlefieldAndReturn(player1, new RobeOfStars());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, 1, null, first.getId());
        harness.passBothPriorities();
        assertThat(robe.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, 1, null, second.getId());
        harness.passBothPriorities();
        assertThat(robe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    @Test
    void unattachedProjectionDoesNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent robe = harness.addToBattlefieldAndReturn(player1, new RobeOfStars());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, robe);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void projectionStillPhasesOutCreatureAfterRobeIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent robe = addRobeAttachedTo(creature);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 0, null, null);

        harness.setHand(player2, java.util.List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, robe.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(robe.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
    }

    @Test
    void indirectlyPhasedRobeWaitsForOpposingCreatureControllerUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent robe = addRobeAttachedTo(creature);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(robe);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);

        advanceToUpkeep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(robe);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(robe.getAttachedTo()).isEqualTo(creature.getId());
    }
}
