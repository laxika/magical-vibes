package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LavastepRaider.class)
class LavastepRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{R} gives Lavastep Raider +2/+0 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent raider = addReadyRaider(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isEqualTo(2);
        assertThat(raider.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Lavastep Raider's temporary boost wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent raider = addReadyRaider(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raider.getPowerModifier()).isZero();
        assertThat(raider.getToughnessModifier()).isZero();
    }

    private Permanent addReadyRaider(Player player) {
        Permanent permanent = new Permanent(new LavastepRaider());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
