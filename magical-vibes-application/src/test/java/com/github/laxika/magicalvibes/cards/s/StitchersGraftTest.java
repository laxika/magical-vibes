package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StitchersGraftTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent graft = addGraftReady(player1);
        graft.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking with the equipped creature keeps it from untapping next untap step")
    void attackTriggerLocksEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent graft = addGraftReady(player1);
        graft.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
        assertThat(graft.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("An unequipped creature attacking is not locked")
    void attackWithoutEquipmentDoesNotLock() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addGraftReady(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Re-equipping sacrifices the previously equipped creature")
    void reEquipSacrificesPreviousCreature() {
        Permanent graft = addGraftReady(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());
        graft.setAttachedTo(creature1.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(graft.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature1.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equipping from unattached state sacrifices nothing")
    void equippingFromUnattachedDoesNotSacrifice() {
        addGraftReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addGraftReady(Player player) {
        Permanent perm = new Permanent(new StitchersGraft());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
