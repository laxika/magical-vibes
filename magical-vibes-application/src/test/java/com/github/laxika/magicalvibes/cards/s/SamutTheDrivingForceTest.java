package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SamutTheDrivingForceTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +X/+0 based on your speed")
    void boostsOtherCreaturesYouControlBySpeed() {
        Permanent samut = addCreatureReady(player1, new SamutTheDrivingForce());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 3);

        assertThat(gqs.getEffectivePower(gd, samut)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature spells you cast cost X less based on your speed")
    void reducesNoncreatureSpellCostsBySpeed() {
        harness.addToBattlefield(player1, new SamutTheDrivingForce());
        gd.playerSpeeds.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Divination.class);
    }

    @Test
    @DisplayName("Creature spells do not receive the cost reduction")
    void doesNotReduceCreatureSpellCosts() {
        harness.addToBattlefield(player1, new SamutTheDrivingForce());
        gd.playerSpeeds.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
