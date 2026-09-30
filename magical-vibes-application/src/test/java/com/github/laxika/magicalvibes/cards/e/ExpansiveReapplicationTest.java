package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpansiveReapplication.class, GrizzlyBears.class})
class ExpansiveReapplicationTest extends BaseCardTest {

    @Test
    void countersTargetSpellAndConjuresPermanentCreatureWithMatchingManaValue() {
        GrizzlyBears bears = castTargetSpell();

        harness.castInstant(player1, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(permanent.getCard().getManaValue()).isEqualTo(1);
                });
    }

    @Test
    void cannotCastWithZeroX() {
        GrizzlyBears bears = castTargetSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must be at least 1");
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new ExpansiveReapplication()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private GrizzlyBears castTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new ExpansiveReapplication()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        return bears;
    }
}
