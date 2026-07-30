package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecondGuessTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the second spell cast this turn")
    void countersSecondSpell() {
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(new LightningBolt(), second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SecondGuess()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.castCreature(player1, 0);

        harness.castInstant(player2, 0, second.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target the third spell cast this turn")
    void cannotTargetThirdSpell() {
        LightningBolt third = new LightningBolt();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), third));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.setHand(player2, List.of(new SecondGuess()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, third.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the only spell cast this turn")
    void cannotTargetFirstSpellWhenAlone() {
        GrizzlyBears only = new GrizzlyBears();
        harness.setHand(player1, List.of(only));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SecondGuess()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, only.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
