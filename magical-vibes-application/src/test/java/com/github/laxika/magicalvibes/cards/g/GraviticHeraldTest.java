package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraviticHerald.class, GrizzlyBears.class, Plains.class})
class GraviticHeraldTest extends BaseCardTest {

    @Test
    void seeksNonlandPermanentWithManaValueThreeOrLessAndGrantsWarp() {
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(grizzlyBears, plains));
        harness.setHand(player1, List.of(new GraviticHerald()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(grizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.cardsGrantedWarpUntilEndOfTurn).containsExactly(grizzlyBears.getId());

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent warpedGrizzly = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(warpedGrizzly.isCastWithWarp()).isTrue();
        assertThat(gd.spellWarpedThisTurn).isTrue();
    }
}
