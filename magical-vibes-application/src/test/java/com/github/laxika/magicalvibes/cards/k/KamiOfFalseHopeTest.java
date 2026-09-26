package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfFalseHope.class, GnarledMass.class, FirstVolley.class})
class KamiOfFalseHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Kami of False Hope prevents all combat damage this turn")
    void sacrificePreventsCombatDamage() {
        harness.addToBattlefield(player1, new KamiOfFalseHope());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kami of False Hope");
        harness.assertInGraveyard(player1, "Kami of False Hope");
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage after the sacrifice")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new KamiOfFalseHope());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new GnarledMass());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The ability prevents combat damage but not noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.addToBattlefield(player1, new KamiOfFalseHope());
        Permanent target = addCreatureReady(player1, new GnarledMass());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }
}
