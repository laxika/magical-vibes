package com.github.laxika.magicalvibes.cards.g;

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

class GhorClanWreckerTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing the Riot counter gives Ghor-Clan Wrecker a +1/+1 counter")
    void riotAddsCounter() {
        Permanent wrecker = castWrecker(true);

        assertThat(wrecker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, wrecker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wrecker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wrecker, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Choosing Riot haste gives Ghor-Clan Wrecker lasting haste")
    void riotAddsPersistentHaste() {
        Permanent wrecker = castWrecker(false);

        assertThat(gqs.hasKeyword(gd, wrecker, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wrecker, Keyword.HASTE)).isTrue();
    }

    private Permanent castWrecker(boolean chooseCounter) {
        harness.setHand(player1, List.of(new GhorClanWrecker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, chooseCounter);

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GhorClanWrecker)
                .findFirst()
                .orElseThrow();
    }
}
