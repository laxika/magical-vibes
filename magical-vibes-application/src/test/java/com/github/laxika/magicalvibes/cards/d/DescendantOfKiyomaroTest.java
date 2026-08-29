package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescendantOfKiyomaroTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+2 and grants the combat-damage life trigger when ahead in hand size")
    void getsBonusWhenControllerHasMoreCardsInHand() {
        harness.setHand(player1, List.of(new DescendantOfKiyomaro(), new DescendantOfKiyomaro()));
        harness.setHand(player2, List.of(new DescendantOfKiyomaro()));
        Permanent descendant = addCreatureReady(player1, new DescendantOfKiyomaro());

        assertThat(gqs.getEffectivePower(gd, descendant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, descendant)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not get the bonus when hand sizes are tied")
    void noBonusWhenHandSizesAreTied() {
        harness.setHand(player1, List.of(new DescendantOfKiyomaro()));
        harness.setHand(player2, List.of(new DescendantOfKiyomaro()));
        Permanent descendant = addCreatureReady(player1, new DescendantOfKiyomaro());

        assertThat(gqs.getEffectivePower(gd, descendant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, descendant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not get the bonus when an opponent has more cards in hand")
    void noBonusWhenOpponentHasMoreCards() {
        harness.setHand(player1, List.of(new DescendantOfKiyomaro()));
        harness.setHand(player2, List.of(new DescendantOfKiyomaro(), new DescendantOfKiyomaro()));
        Permanent descendant = addCreatureReady(player1, new DescendantOfKiyomaro());

        assertThat(gqs.getEffectivePower(gd, descendant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, descendant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gains 3 life after dealing combat damage while the bonus is active")
    void gainsLifeAfterCombatDamage() {
        harness.setHand(player1, List.of(new DescendantOfKiyomaro()));
        harness.setHand(player2, List.of());
        Permanent descendant = addCreatureReady(player1, new DescendantOfKiyomaro());
        descendant.setAttacking(true);
        harness.setLife(player1, 10);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Does not have the life trigger when the controller is not ahead in hand size")
    void noLifeTriggerWhenHandSizesAreTied() {
        harness.setHand(player1, List.of(new DescendantOfKiyomaro()));
        harness.setHand(player2, List.of(new DescendantOfKiyomaro()));
        Permanent descendant = addCreatureReady(player1, new DescendantOfKiyomaro());
        descendant.setAttacking(true);
        harness.setLife(player1, 10);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player, DescendantOfKiyomaro card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
