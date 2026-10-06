package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingRendhorn.class})
class RampagingRendhornTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing the Riot counter gives Rampaging Rendhorn a +1/+1 counter")
    void riotAddsCounter() {
        Permanent rendhorn = castRendhorn(true);

        assertThat(rendhorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, rendhorn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, rendhorn)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, rendhorn, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Choosing Riot haste gives Rampaging Rendhorn lasting haste")
    void riotAddsPersistentHaste() {
        Permanent rendhorn = castRendhorn(false);

        assertThat(gqs.hasKeyword(gd, rendhorn, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rendhorn, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Riot is chosen before Rampaging Rendhorn enters the battlefield")
    void riotChoicePrecedesEntry() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new RampagingRendhorn(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotOnBattlefield(player1, "Rampaging Rendhorn");

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Rampaging Rendhorn");
        Permanent rendhorn = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(rendhorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, rendhorn, Keyword.HASTE)).isTrue();
    }

    private Permanent castRendhorn(boolean chooseCounter) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new RampagingRendhorn(), "{4}{G}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, chooseCounter);

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RampagingRendhorn)
                .findFirst()
                .orElseThrow();
    }
}
