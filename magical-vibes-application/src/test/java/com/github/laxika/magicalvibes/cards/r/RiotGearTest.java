package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiotGearTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Riot Gear puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new RiotGear()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Riot Gear") && !p.isAttached());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent gear = addReady(player1, new RiotGear());
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Unequipped creature is unaffected")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent other = addReady(player1, new GrizzlyBears());
        Permanent gear = addReady(player1, new RiotGear());
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost is lost when Riot Gear leaves the battlefield")
    void boostLostWhenEquipmentRemoved() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent gear = addReady(player1, new RiotGear());
        gear.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(gear);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {2} moves Riot Gear to another creature")
    void equipMovesGear() {
        Permanent gear = addReady(player1, new RiotGear());
        Permanent creature1 = addReady(player1, new GrizzlyBears());
        Permanent creature2 = addReady(player1, new GrizzlyBears());
        gear.setAttachedTo(creature1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(4);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
