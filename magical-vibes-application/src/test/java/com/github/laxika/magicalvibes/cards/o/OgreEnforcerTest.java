package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OgreEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Survives lethal damage split across two Shock sources")
    void survivesDamageFromTwoSources() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, enforcer.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, enforcer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(enforcer.getId()));
        assertThat(enforcer.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Dies when a single source deals lethal damage")
    void diesToSingleSourceLethalDamage() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        harness.setHand(player1, List.of(new LightningBlast()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, enforcer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(enforcer.getId()));
    }

    @Test
    @DisplayName("Still dies to 0 toughness")
    void diesToZeroToughness() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        enforcer.setToughnessModifier(-4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(enforcer.getId()));
    }

    @Test
    @DisplayName("Survives when two sources each mark half of lethal damage")
    void survivesSplitMarkedDamageFromTwoSources() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        enforcer.addMarkedDamage(UUID.randomUUID(), 2);
        enforcer.addMarkedDamage(UUID.randomUUID(), 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(enforcer.getId()));
    }
}
