package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkitterbeamBattalionTest extends BaseCardTest {

    @Test
    void castCreatesTwoTokenCopies() {
        harness.setHand(player1, List.of(new SkitterbeamBattalion()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(battalions(player1)).hasSize(3);
        assertThat(battalions(player1).stream().filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2);
    }

    @Test
    void prototypeCastCreatesTwoPrototypeTokenCopies() {
        harness.setHand(player1, List.of(new SkitterbeamBattalion()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(battalions(player1)).hasSize(3);
        assertThat(battalions(player1)).allSatisfy(permanent -> {
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
            assertThat(gqs.getEffectiveColors(gd, permanent)).containsExactly(CardColor.RED);
        });
    }

    @Test
    void enteringWithoutBeingCastDoesNotCreateTokenCopies() {
        harness.addToBattlefield(player1, new SkitterbeamBattalion());

        assertThat(battalions(player1)).hasSize(1);
    }

    private List<Permanent> battalions(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Skitterbeam Battalion"))
                .toList();
    }
}
