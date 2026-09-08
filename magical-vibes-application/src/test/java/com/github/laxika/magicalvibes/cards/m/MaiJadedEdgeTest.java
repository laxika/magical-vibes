package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaiJadedEdge.class, Shock.class})
class MaiJadedEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell gives Mai +1/+1 until end of turn")
    void noncreatureSpellPumpsMai() {
        Permanent mai = addMai();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mai)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mai)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mai)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mai)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exhaust puts a double-strike counter on Mai")
    void exhaustPutsDoubleStrikeCounterOnMai() {
        Permanent mai = addMai();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(mai.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mai, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Mai's exhaust ability can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addMai();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addMai() {
        Permanent mai = harness.addToBattlefieldAndReturn(player1, new MaiJadedEdge());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return mai;
    }
}
