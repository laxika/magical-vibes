package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImprovisedArsenal.class, GrizzlyBears.class, Ornithopter.class})
class ImprovisedArsenalTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 for each artifact its controller controls")
    void equippedCreatureScalesWithControlledArtifacts() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent arsenal = harness.addToBattlefieldAndReturn(player1, new ImprovisedArsenal());
        arsenal.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.addToBattlefield(player1, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip attaches Improvised Arsenal to a creature you control")
    void equipAttachesToCreature() {
        Permanent arsenal = harness.addToBattlefieldAndReturn(player1, new ImprovisedArsenal());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(arsenal.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The activated ability creates a token copy of Improvised Arsenal")
    void createsTokenCopy() {
        harness.addToBattlefield(player1, new ImprovisedArsenal());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }
}
