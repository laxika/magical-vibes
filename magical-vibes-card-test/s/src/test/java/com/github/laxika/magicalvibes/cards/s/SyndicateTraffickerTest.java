package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyndicateTrafficker.class, RenegadeFreighter.class})
class SyndicateTraffickerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact puts a counter on Syndicate Trafficker and grants indestructible")
    void sacrificingArtifactPutsCounterAndGrantsIndestructible() {
        Permanent trafficker = addReadyTrafficker(player1);
        harness.addToBattlefield(player1, new RenegadeFreighter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(trafficker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, trafficker, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Renegade Freighter");
        harness.assertOnBattlefield(player1, "Syndicate Trafficker");
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn while the counter remains")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent trafficker = addReadyTrafficker(player1);
        harness.addToBattlefield(player1, new RenegadeFreighter());
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
        Permanent trafficker = harness.addToBattlefieldAndReturn(player, new SyndicateTrafficker());
        trafficker.setSummoningSick(false);
        return trafficker;
    }

    @Test
    void canActivateWhileSummoningSickAndTappedButBenefitsWaitForResolution() {
        Permanent trafficker = harness.addToBattlefieldAndReturn(player1, new SyndicateTrafficker());
        trafficker.setSummoningSick(true);
        trafficker.tap();
        harness.addToBattlefield(player1, new RenegadeFreighter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Renegade Freighter");
        harness.assertNotOnBattlefield(player1, "Renegade Freighter");
        assertThat(trafficker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, trafficker, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(trafficker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, trafficker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void cannotActivateWithoutAnArtifactYouControl() {
        Permanent trafficker = addReadyTrafficker(player1);
        harness.addToBattlefield(player2, new RenegadeFreighter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Renegade Freighter");
        harness.assertOnBattlefield(player1, "Syndicate Trafficker");
        assertThat(trafficker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        addReadyTrafficker(player1);
        harness.addToBattlefield(player1, new RenegadeFreighter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Renegade Freighter");
        harness.assertNotInGraveyard(player1, "Renegade Freighter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedActivationsAccumulateCounters() {
        Permanent trafficker = addReadyTrafficker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new RenegadeFreighter());
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(trafficker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, trafficker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
