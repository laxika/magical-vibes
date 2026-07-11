package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmoeboidChangelingTest extends BaseCardTest {

    /** Adds Amoeboid Changeling at battlefield index 0, ready to tap. */
    private void addAmoeboidReady() {
        Permanent amoeboid = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());
        amoeboid.setSummoningSick(false);
    }

    private Permanent find(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    // ===== Ability 1: gains all creature types =====

    @Test
    @DisplayName("Ability 1 makes a non-Goblin count as a Goblin, so Goblin King buffs it")
    void gainAllCreatureTypesTriggersTribalBuff() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = find("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2); // not a Goblin yet

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3); // now every creature type incl. Goblin
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    // ===== Ability 2: loses all creature types =====

    @Test
    @DisplayName("Ability 2 strips a base Goblin's creature types, removing Goblin King's buff")
    void loseAllCreatureTypesRemovesTribalBuff() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());

        Permanent goblin = find("Goblin Elite Infantry");
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3); // 2/2 + Goblin King

        UUID targetId = harness.getPermanentId(player1, "Goblin Elite Infantry");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2); // no longer a Goblin
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lost creature types return at end of turn")
    void loseAllCreatureTypesWearsOff() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());

        Permanent goblin = find("Goblin Elite Infantry");
        UUID targetId = harness.getPermanentId(player1, "Goblin Elite Infantry");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);

        goblin.resetModifiers(); // end-of-turn cleanup

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3); // Goblin again
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Abilities can only target creatures")
    void cannotTargetNonCreature() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new Mountain());

        UUID targetId = harness.getPermanentId(player1, "Mountain");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
