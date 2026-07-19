package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KraniocerosTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+3 to Kranioceros")
    void resolvingAbilityBoostsToughness() {
        addKraniocerosReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent kranioceros = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(kranioceros.getEffectivePower()).isEqualTo(5);
        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(5);
        assertThat(kranioceros.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated multiple times, boosts stack")
    void canActivateMultipleTimes() {
        addKraniocerosReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent kranioceros = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addKraniocerosReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent kranioceros = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(kranioceros.getToughnessModifier()).isEqualTo(0);
        assertThat(kranioceros.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addKraniocerosReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private Permanent addKraniocerosReady(Player player) {
        Kranioceros card = new Kranioceros();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
