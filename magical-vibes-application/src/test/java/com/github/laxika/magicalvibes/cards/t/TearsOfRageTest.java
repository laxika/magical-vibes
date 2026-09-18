package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LeoninShikari;
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

@CardUsed({TearsOfRage.class, LeoninShikari.class})
class TearsOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts attacking creatures by their count and sacrifices them at the next end step")
    void boostsAndSacrificesAttackingCreatures() {
        Permanent attackerOne = addCreatureReady(player1, new LeoninShikari());
        Permanent attackerTwo = addCreatureReady(player1, new LeoninShikari());
        Permanent nonAttacker = addCreatureReady(player1, new LeoninShikari());
        harness.setHand(player1, List.of(new TearsOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0, 1));
        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, attackerOne)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, attackerTwo)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attackerOne)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attackerTwo)).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(attackerOne, attackerTwo)
                .contains(nonAttacker);
    }

    @Test
    @DisplayName("Sacrifices creatures captured at resolution even if they stop attacking")
    void sacrificesCapturedAttackersEvenIfTheyStopAttacking() {
        Permanent attacker = addCreatureReady(player1, new LeoninShikari());
        harness.setHand(player1, List.of(new TearsOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player1, 0);
        attacker.setAttacking(false);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("Can only be cast during the declare attackers step")
    void cannotCastOutsideDeclareAttackersStep() {
        harness.setHand(player1, List.of(new TearsOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
