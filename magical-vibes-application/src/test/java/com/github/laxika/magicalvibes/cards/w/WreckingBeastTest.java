package com.github.laxika.magicalvibes.cards.w;

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

class WreckingBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing the Riot counter gives Wrecking Beast a +1/+1 counter")
    void riotAddsCounter() {
        Permanent beast = castBeast(true);

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Choosing Riot haste gives Wrecking Beast lasting haste")
    void riotAddsPersistentHaste() {
        Permanent beast = castBeast(false);

        assertThat(gqs.hasKeyword(gd, beast, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.HASTE)).isTrue();
    }

    private Permanent castBeast(boolean chooseCounter) {
        harness.setHand(player1, List.of(new WreckingBeast()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, chooseCounter);

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof WreckingBeast)
                .findFirst()
                .orElseThrow();
    }
}
