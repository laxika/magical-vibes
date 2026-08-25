package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndeadWarchief.class, Gravedigger.class, GrizzlyBears.class})
class UndeadWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Undead Warchief boosts itself")
    void boostsItself() {
        Permanent warchief = addCreatureReady(player1, new UndeadWarchief());

        assertThat(gqs.getEffectivePower(gd, warchief)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warchief)).isEqualTo(2);
    }

    @Test
    @DisplayName("Zombie creatures you control get +2/+1")
    void boostsOwnZombies() {
        Permanent zombie = addCreatureReady(player1, new Gravedigger());
        addCreatureReady(player1, new UndeadWarchief());

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost non-Zombie creatures or an opponent's Zombies")
    void onlyBoostsOwnZombies() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentZombie = addCreatureReady(player2, new Gravedigger());
        addCreatureReady(player1, new UndeadWarchief());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Zombie spells you cast cost {1} less to cast")
    void reducesOwnZombieSpellCost() {
        addCreatureReady(player1, new UndeadWarchief());
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Zombie creature spells are not reduced")
    void doesNotReduceNonZombieSpellCost() {
        addCreatureReady(player1, new UndeadWarchief());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The cost reduction does not apply to an opponent's Zombie spells")
    void doesNotReduceOpponentsZombieSpellCost() {
        addCreatureReady(player1, new UndeadWarchief());
        harness.setHand(player2, List.of(new Gravedigger()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
