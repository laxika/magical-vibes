package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MesaFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+1 to Mesa Falcon")
    void resolvingAbilityBoostsToughness() {
        addFalconReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent falcon = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(falcon.getEffectivePower()).isEqualTo(1);
        assertThat(falcon.getEffectiveToughness()).isEqualTo(2);
        assertThat(falcon.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated multiple times, boosts stack")
    void canActivateMultipleTimes() {
        addFalconReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent falcon = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(falcon.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addFalconReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent falcon = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(falcon.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(falcon.getToughnessModifier()).isEqualTo(0);
        assertThat(falcon.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addFalconReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private Permanent addFalconReady(Player player) {
        MesaFalcon card = new MesaFalcon();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
