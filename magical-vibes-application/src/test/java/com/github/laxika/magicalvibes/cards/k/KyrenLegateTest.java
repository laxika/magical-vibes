package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KyrenLegate.class, Mountain.class, Plains.class})
class KyrenLegateTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free when an opponent controls a Plains and you control a Mountain")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new KyrenLegate()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kyren Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Haste lets Kyren Legate attack the turn it enters")
    void hasteAllowsImmediateAttack() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new KyrenLegate()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        Permanent kyrenLegate = findPermanent(player1, "Kyren Legate");
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kyrenLegate)));
        resolveCombat();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without an opponent-controlled Plains")
    void alternateCostRequiresOpponentPlains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new KyrenLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without a controller-controlled Mountain")
    void alternateCostRequiresControllerMountain() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new KyrenLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally for its mana cost")
    void castsNormally() {
        harness.castFromHand(player1, new KyrenLegate(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kyren Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
