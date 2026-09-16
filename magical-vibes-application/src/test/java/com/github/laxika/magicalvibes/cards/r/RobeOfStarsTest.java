package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RobeOfStars.class, GrizzlyBears.class})
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
}
