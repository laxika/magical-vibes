package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MountainBandit.class})
class MountainBanditTest extends BaseCardTest {

    @Test
    void canAttackTheTurnItEntersTheBattlefield() {
        MountainBandit card = new MountainBandit();
        harness.castFromHand(player1, card, "{R}");
        harness.passBothPriorities();

        Permanent bandit = findPermanent(player1, "Mountain Bandit");
        assertThat(bandit.isSummoningSick()).isTrue();

        declareAttackers(List.of(0));

        assertThat(bandit.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void hasteDoesNotAllowAttackingWhileTapped() {
        harness.castFromHand(player1, new MountainBandit(), "{R}");
        harness.passBothPriorities();
        Permanent bandit = findPermanent(player1, "Mountain Bandit");
        bandit.setTapped(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
