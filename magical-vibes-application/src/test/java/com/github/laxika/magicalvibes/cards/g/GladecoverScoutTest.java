package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GladecoverScout.class, Shock.class, GideonsLawkeeper.class, DayOfJudgment.class})
class GladecoverScoutTest extends BaseCardTest {

    @Test
    void opponentCannotTargetScoutWithSpell() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new GladecoverScout());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, scout.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Gladecover Scout");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanTargetScoutWithSpell() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new GladecoverScout());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, scout.getId());

        harness.assertNotOnBattlefield(player1, "Gladecover Scout");
        harness.assertInGraveyard(player1, "Gladecover Scout");
    }

    @Test
    void opponentCannotTargetScoutWithAbility() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new GladecoverScout());
        addCreatureReady(player2, new GideonsLawkeeper());
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, scout.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(scout.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanTargetScoutWithAbility() {
        addCreatureReady(player1, new GideonsLawkeeper());
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new GladecoverScout());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isTrue();
    }

    @Test
    void hexproofDoesNotPreventOpponentsUntargetedRemoval() {
        harness.addToBattlefield(player1, new GladecoverScout());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DayOfJudgment()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertNotOnBattlefield(player1, "Gladecover Scout");
        harness.assertInGraveyard(player1, "Gladecover Scout");
    }
}
