package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrakeHatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Puts incubation counters on itself equal to combat damage dealt to a player")
    void combatDamageAddsIncubationCounters() {
        Permanent hatcher = addReadyHatcher();
        hatcher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        hatcher.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isZero();

        harness.passBothPriorities();

        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing three incubation counters creates a 2/2 blue Drake with flying")
    void removesCountersAndCreatesDrake() {
        Permanent hatcher = addReadyHatcher();
        hatcher.setCounterCount(CounterType.INCUBATION, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hatcher.getCounterCount(CounterType.INCUBATION)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getColor() == CardColor.BLUE
                        && permanent.getCard().getPower() == 2
                        && permanent.getCard().getToughness() == 2
                        && permanent.getCard().getSubtypes().contains(CardSubtype.DRAKE)
                        && permanent.getCard().getKeywords().contains(Keyword.FLYING));
    }

    @Test
    @DisplayName("Cannot activate without three incubation counters")
    void cannotActivateWithoutEnoughCounters() {
        addReadyHatcher();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHatcher() {
        Permanent hatcher = new Permanent(new DrakeHatcher());
        hatcher.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hatcher);
        return hatcher;
    }
}
