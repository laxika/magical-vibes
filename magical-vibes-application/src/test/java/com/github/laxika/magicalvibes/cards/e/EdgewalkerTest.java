package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BondsOfFaith;
import com.github.laxika.magicalvibes.cards.c.ClericOfTheForwardOrder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Edgewalker.class, ClericOfTheForwardOrder.class, BondsOfFaith.class, GrizzlyBears.class})
class EdgewalkerTest extends BaseCardTest {

    @Test
    void reducesColoredManaOfClericSpells() {
        harness.addToBattlefield(player1, new Edgewalker());
        harness.setHand(player1, List.of(new ClericOfTheForwardOrder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceGenericManaOfClericSpells() {
        harness.addToBattlefield(player1, new Edgewalker());
        harness.setHand(player1, List.of(new ClericOfTheForwardOrder()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceNonClericSpells() {
        harness.addToBattlefield(player1, new Edgewalker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }
}
