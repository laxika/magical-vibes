package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonlordsServant.class, DragonEgg.class, GrizzlyBears.class})
class DragonlordsServantTest extends BaseCardTest {

    @Test
    void dragonSpellsCostOneLess() {
        harness.addToBattlefield(player1, new DragonlordsServant());
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dragon Egg");
    }

    @Test
    void twoServantsStackTheirReductions() {
        harness.addToBattlefield(player1, new DragonlordsServant());
        harness.addToBattlefield(player1, new DragonlordsServant());
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dragon Egg");
    }

    @Test
    void doesNotReduceNonDragonCreatureSpells() {
        harness.addToBattlefield(player1, new DragonlordsServant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceOpponentsDragonSpells() {
        harness.addToBattlefield(player1, new DragonlordsServant());
        harness.setHand(player2, List.of(new DragonEgg()));
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
