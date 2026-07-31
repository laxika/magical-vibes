package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhantasmalSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep adds a +1/+1 counter and the sphere survives when the upkeep cost is paid")
    void payingUpkeepKeepsSphere() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new PhantasmalSphere());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(sphere.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sphere);
    }

    @Test
    @DisplayName("Declining the upkeep cost sacrifices the sphere and gives an opponent an X/X flying Orb")
    void decliningUpkeepSacrificesAndGivesOrb() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new PhantasmalSphere());
        sphere.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Third counter goes on before the payment is sized, so the cost is {3}.
        assertThat(sphere.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphere);

        Permanent orb = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Orb"))
                .findFirst().orElseThrow();
        assertThat(orb.getEffectivePower()).isEqualTo(3);
        assertThat(orb.getEffectiveToughness()).isEqualTo(3);
    }
}
