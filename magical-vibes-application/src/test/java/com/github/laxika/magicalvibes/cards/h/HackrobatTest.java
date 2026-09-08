package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HackrobatTest extends BaseCardTest {

    @Test
    @DisplayName("The black ability grants deathtouch until end of turn")
    void grantsDeathtouch() {
        Permanent hackrobat = addHackrobat(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hackrobat, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The red ability gives Hackrobat +2/-2 until end of turn")
    void boostsPowerAndReducesToughness() {
        Permanent hackrobat = addHackrobat(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hackrobat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hackrobat)).isEqualTo(1);
    }

    @Test
    @DisplayName("Hackrobat's activated abilities wear off at end of turn")
    void abilitiesWearOffAtEndOfTurn() {
        Permanent hackrobat = addHackrobat(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hackrobat, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hackrobat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackrobat)).isEqualTo(3);
    }

    private Permanent addHackrobat(Player player) {
        Permanent permanent = new Permanent(new Hackrobat());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
