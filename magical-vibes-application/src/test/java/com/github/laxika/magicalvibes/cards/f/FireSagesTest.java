package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FireSages.class)
class FireSagesTest extends BaseCardTest {

    @Test
    @DisplayName("Firebending adds one red mana until end of combat")
    void firebendingAddsManaUntilEndOfCombat() {
        Permanent fireSages = addReadyFireSages();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(fireSages.isTapped()).isTrue();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The activated ability puts a +1/+1 counter on Fire Sages")
    void activatedAbilityAddsCounter() {
        Permanent fireSages = addReadyFireSages();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fireSages.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyFireSages() {
        Permanent fireSages = new Permanent(new FireSages());
        fireSages.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fireSages);
        return fireSages;
    }
}
