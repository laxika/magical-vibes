package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thundermare.class, GrizzlyBears.class})
class ThundermareTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all other creatures on both battlefields")
    void etbTapsAllOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not tap Thundermare itself")
    void etbDoesNotTapSelf() {
        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Thundermare").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Haste allows Thundermare to attack the turn it enters")
    void hasteAllowsAttackingImmediately() {
        harness.castFromHand(player1, new Thundermare(), "{5}{R}");
        resolveAllTriggers();

        Permanent thundermare = findPermanent(player1, "Thundermare");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(thundermare)));

        assertThat(thundermare.isAttackedThisTurn()).isTrue();
    }
}
