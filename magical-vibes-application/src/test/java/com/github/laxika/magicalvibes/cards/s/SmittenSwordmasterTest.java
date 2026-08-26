package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CurryFavor;
import com.github.laxika.magicalvibes.cards.m.SmittenSwordmaster;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmittenSwordmaster.class, CurryFavor.class, WhiteKnight.class})
class SmittenSwordmasterTest extends BaseCardTest {

    @Test
    void adventureGainsAndLosesLifeForEachKnightControlled() {
        harness.addToBattlefield(player1, new WhiteKnight());
        harness.addToBattlefield(player1, new WhiteKnight());
        SmittenSwordmaster card = new SmittenSwordmaster();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCountsOnlyKnightsControlledByCaster() {
        harness.addToBattlefield(player1, new WhiteKnight());
        harness.addToBattlefield(player2, new WhiteKnight());
        harness.setHand(player1, List.of(new SmittenSwordmaster()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
