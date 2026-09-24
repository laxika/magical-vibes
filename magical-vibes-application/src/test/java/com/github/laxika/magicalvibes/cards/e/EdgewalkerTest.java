package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.d.DaruSpiritualist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Edgewalker.class, AvenFarseer.class, DaruSpiritualist.class})
class EdgewalkerTest extends BaseCardTest {

    @Test
    void reducesColoredManaOfClericSpells() {
        harness.addToBattlefield(player1, new Edgewalker());
        harness.setHand(player1, List.of(new DaruSpiritualist()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceGenericManaOfClericSpells() {
        harness.addToBattlefield(player1, new Edgewalker());
        harness.setHand(player1, List.of(new DaruSpiritualist()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceNonClericSpells() {
        harness.addToBattlefield(player1, new Edgewalker());
        harness.setHand(player1, List.of(new AvenFarseer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesBothWhiteAndBlackManaOfClericSpells() {
        harness.addToBattlefield(player1, new Edgewalker());
        harness.setHand(player1, List.of(new Edgewalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceOpponentClericSpells() {
        harness.addToBattlefield(player1, new Edgewalker());
        harness.setHand(player2, List.of(new DaruSpiritualist()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
