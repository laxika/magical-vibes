package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmorphousAxe.class, GrizzlyBears.class})
class AmorphousAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+0 and every creature type")
    void equippedCreatureGetsBoostAndEveryCreatureType() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Amorphous Axe can equip a creature for {3}")
    void equipsCreature() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equipped creature loses the Axe's effects when it is removed")
    void effectsEndWhenAxeLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(axe);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.CHANGELING)).isFalse();
    }

    private Permanent addAxeReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent axe = new Permanent(new AmorphousAxe());
        axe.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(axe);
        return axe;
    }
}
