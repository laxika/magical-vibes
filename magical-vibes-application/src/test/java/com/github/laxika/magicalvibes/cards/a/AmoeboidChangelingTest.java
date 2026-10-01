package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KithkinHealer;
import com.github.laxika.magicalvibes.cards.s.SecludedGlen;
import com.github.laxika.magicalvibes.cards.w.WizenedCenn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmoeboidChangeling.class, WizenedCenn.class, Aethersnipe.class, KithkinHealer.class, SecludedGlen.class})
class AmoeboidChangelingTest extends BaseCardTest {

    /** Adds Amoeboid Changeling at battlefield index 0, ready to tap. */
    private void addAmoeboidReady() {
        Permanent amoeboid = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());
        amoeboid.setSummoningSick(false);
    }

    private Permanent find(String name) {
        return findPermanent(player1, name);
    }

    // ===== Ability 1: gains all creature types =====

    @Test
    @DisplayName("Ability 1 makes a non-Kithkin count as a Kithkin, so Wizened Cenn buffs it")
    void gainAllCreatureTypesTriggersTribalBuff() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new Aethersnipe());

        Permanent aethersnipe = find("Aethersnipe");
        assertThat(gqs.getEffectivePower(gd, aethersnipe)).isEqualTo(4); // not a Kithkin yet

        UUID targetId = harness.getPermanentId(player1, "Aethersnipe");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aethersnipe)).isEqualTo(5); // now every creature type incl. Kithkin
        assertThat(gqs.getEffectiveToughness(gd, aethersnipe)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ability 1 can target an opponent's creature")
    void gainAllCreatureTypesCanTargetOpponentCreature() {
        addAmoeboidReady();
        harness.addToBattlefield(player2, new WizenedCenn());
        harness.addToBattlefield(player2, new Aethersnipe());

        Permanent aethersnipe = findPermanent(player2, "Aethersnipe");
        assertThat(gqs.getEffectivePower(gd, aethersnipe)).isEqualTo(4); // not a Kithkin yet

        UUID targetId = harness.getPermanentId(player2, "Aethersnipe");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aethersnipe)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aethersnipe)).isEqualTo(5);
    }

    // ===== Ability 2: loses all creature types =====

    @Test
    @DisplayName("Ability 2 strips a base Kithkin's creature types, removing Wizened Cenn's buff")
    void loseAllCreatureTypesRemovesTribalBuff() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new KithkinHealer());

        Permanent kithkin = find("Kithkin Healer");
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(3); // 2/2 + Wizened Cenn

        UUID targetId = harness.getPermanentId(player1, "Kithkin Healer");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2); // no longer a Kithkin
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lost creature types return at end of turn")
    void loseAllCreatureTypesWearsOff() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new KithkinHealer());

        Permanent kithkin = find("Kithkin Healer");
        UUID targetId = harness.getPermanentId(player1, "Kithkin Healer");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2);

        kithkin.resetModifiers(); // end-of-turn cleanup

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(3); // Kithkin again
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Abilities can only target creatures")
    void cannotTargetNonCreature() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new SecludedGlen());

        UUID targetId = harness.getPermanentId(player1, "Secluded Glen");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
