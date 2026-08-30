package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiamondPickAxe.class, GrizzlyBears.class})
class DiamondPickAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pickAxe = addPickAxeReady(player1);
        pickAxe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking with the equipped creature creates a Treasure token")
    void attackCreatesTreasureToken() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        pickAxe.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("An unattached Diamond Pick-Axe does not create a Treasure when a creature attacks")
    void unattachedPickAxeDoesNotTrigger() {
        addPickAxeReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Equip {2} attaches Diamond Pick-Axe to a creature you control")
    void equipAttachesToCreature() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pickAxe.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addPickAxeReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new DiamondPickAxe());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
