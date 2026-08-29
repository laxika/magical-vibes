package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent castRendhorn(boolean chooseCounter) {
        harness.setHand(player1, List.of(new RampagingRendhorn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, chooseCounter);

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RampagingRendhorn)
                .findFirst()
                .orElseThrow();
    }
}
