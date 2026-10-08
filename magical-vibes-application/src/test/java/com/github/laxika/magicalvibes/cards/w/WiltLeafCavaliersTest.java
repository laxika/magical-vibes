package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WiltLeafCavaliers.class})
class WiltLeafCavaliersTest extends BaseCardTest {

    @Test
    void attacksWithoutTappingAndDealsCombatDamage() {
        Permanent cavaliers = addCreatureReady(player1, new WiltLeafCavaliers());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat(player1);

        assertThat(cavaliers.isTapped()).isFalse();
        harness.assertLife(player2, 17);
    }

    @Test
    void vigilanceAlsoWorksWhenSecondPlayerAttacks() {
        Permanent cavaliers = addCreatureReady(player2, new WiltLeafCavaliers());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(cavaliers.isTapped()).isFalse();
        harness.assertLife(player1, 17);
    }

    @Test
    void vigilanceDoesNotAllowTappedCreatureToAttack() {
        Permanent cavaliers = addCreatureReady(player1, new WiltLeafCavaliers());
        cavaliers.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cavaliers.isTapped()).isTrue();
        assertThat(cavaliers.isAttacking()).isFalse();
    }

    @Test
    void vigilanceDoesNotBypassSummoningSickness() {
        Permanent cavaliers = harness.addToBattlefieldAndReturn(player1, new WiltLeafCavaliers());
        cavaliers.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cavaliers.isTapped()).isFalse();
        assertThat(cavaliers.isAttacking()).isFalse();
    }
}
