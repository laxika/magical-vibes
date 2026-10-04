package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarnheimAspirant.class, AngelOfMercy.class, GrizzlyBears.class})
class StarnheimAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("Angel spells you cast cost {2} less")
    void angelSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new StarnheimAspirant());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Angel creature spells are not reduced")
    void nonAngelCreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StarnheimAspirant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to an opponent's Angel spells")
    void opponentAngelSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StarnheimAspirant());
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
