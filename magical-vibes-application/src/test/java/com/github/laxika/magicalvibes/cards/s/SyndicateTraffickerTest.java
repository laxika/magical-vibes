package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyndicateTraffickerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact puts a counter on Syndicate Trafficker and grants indestructible")
    void sacrificingArtifactPutsCounterAndGrantsIndestructible() {
        Permanent trafficker = addReadyTrafficker(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(trafficker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, trafficker, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Syndicate Trafficker");
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn while the counter remains")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent trafficker = addReadyTrafficker(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(trafficker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, trafficker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addReadyTrafficker(Player player) {
        Permanent trafficker = new Permanent(new SyndicateTrafficker());
        trafficker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(trafficker);
        return trafficker;
    }
}
