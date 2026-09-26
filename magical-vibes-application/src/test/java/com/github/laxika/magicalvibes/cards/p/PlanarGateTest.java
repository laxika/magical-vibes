package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlackManaBattery;
import com.github.laxika.magicalvibes.cards.b.BronzeHorse;
import com.github.laxika.magicalvibes.cards.c.CatWarriors;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackManaBattery.class, BronzeHorse.class, CatWarriors.class, PlanarGate.class})
class PlanarGateTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells you cast cost {2} less")
    void creatureSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new PlanarGate());
        harness.castFromHand(player1, new CatWarriors(), "{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(CatWarriors.class);
    }

    @Test
    @DisplayName("Noncreature spells are not reduced")
    void noncreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new PlanarGate());

        assertThatThrownBy(() -> harness.castFromHand(player1, new BlackManaBattery(), "{2}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact creature spells are reduced")
    void artifactCreatureSpellsAreReduced() {
        harness.addToBattlefield(player1, new PlanarGate());

        harness.castFromHand(player1, new BronzeHorse(), "{5}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BronzeHorse.class);
    }

    @Test
    @DisplayName("Planar Gate does not reduce an opponent's creature spells")
    void opponentCreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new PlanarGate());

        assertThatThrownBy(() -> harness.castFromHand(player2, new CatWarriors(), "{G}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }
}
