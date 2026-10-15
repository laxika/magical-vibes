package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DNedainBlade.class, EliteVanguard.class, GrizzlyBears.class})
class DNedainBladeTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusOne() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void humanEquipAttachesForOneMana() {
        Permanent blade = addBladeReady(player1);
        Permanent human = addReadyCreature(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(human.getId());
    }

    @Test
    void humanEquipRejectsNonHuman() {
        addBladeReady(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Human");
    }

    @Test
    void genericEquipAttachesToNonHuman() {
        Permanent blade = addBladeReady(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
    }

    private Permanent addBladeReady(Player player) {
        Permanent blade = new Permanent(new DNedainBlade());
        blade.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blade);
        return blade;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }
}
