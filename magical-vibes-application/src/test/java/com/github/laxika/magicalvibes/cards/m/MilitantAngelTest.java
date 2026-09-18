package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MilitantAngel.class, GrizzlyBears.class})
class MilitantAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Knight per distinct opponent attacked this turn")
    void createsOneKnightPerOpponentAttacked() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0, 1));
        resolveCombat();

        castMilitantAngel(player1);

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
    }

    @Test
    @DisplayName("Creates no Knights when you did not attack this turn")
    void createsNoKnightsWithoutAttacking() {
        castMilitantAngel(player1);

        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    private void castMilitantAngel(Player player) {
        harness.setHand(player, List.of(new MilitantAngel()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
