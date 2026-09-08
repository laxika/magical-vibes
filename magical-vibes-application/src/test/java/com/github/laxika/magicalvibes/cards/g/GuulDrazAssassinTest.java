package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuulDrazAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Guul Draz Assassin's stats and activated ability")
    void levelsUpAtThresholds() {
        Permanent assassin = addCreatureReady(player1, new GuulDrazAssassin());

        assertStats(assassin, 1, 1);

        prepareForLeveling(player1, 4);
        levelUp(player1);
        levelUp(player1);

        assertThat(assassin.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertStats(assassin, 2, 2);

        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertStats(target, 1, 1);
    }

    @Test
    @DisplayName("At level four Guul Draz Assassin gives -4/-4 until end of turn")
    void levelFourAbilityGivesMinusFourMinusFour() {
        Permanent assassin = addCreatureReady(player1, new GuulDrazAssassin());
        Permanent target = addCreatureReady(player2, new HillGiant());

        prepareForLeveling(player1, 8);
        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(assassin.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
    }

    @Test
    @DisplayName("The assassin abilities can target only creatures")
    void abilitiesRequireCreatureTarget() {
        addCreatureReady(player1, new GuulDrazAssassin());
        prepareForLeveling(player1, 2);
        levelUp(player1);
        levelUp(player1);

        Permanent swamp = new Permanent(new Swamp());
        gd.playerBattlefields.get(player2.getId()).add(swamp);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void prepareForLeveling(Player player, int blackMana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLACK, blackMana);
        harness.addMana(player, ManaColor.COLORLESS, blackMana);
    }

    private void levelUp(Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
