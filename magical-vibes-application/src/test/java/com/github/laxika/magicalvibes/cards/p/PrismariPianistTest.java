package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrismariPianist.class, Shock.class, Divination.class, LavaAxe.class, GrizzlyBears.class})
class PrismariPianistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates one Elemental token")
    void instantCreatesOneElemental() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell with mana value 5 creates three Elemental tokens")
    void highManaValueSpellCreatesThreeElementals() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a creature spell does not create Elemental tokens")
    void creatureSpellCreatesNoElementals() {
        harness.addToBattlefield(player1, new PrismariPianist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }
}
