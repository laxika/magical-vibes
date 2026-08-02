package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NightfireGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/3 without a Mountain")
    void noBoostWithoutMountain() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.addToBattlefield(player1, new Forest());

        Permanent giant = findPermanent(player1, "Nightfire Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 while you control a Mountain")
    void boostWithMountain() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.addToBattlefield(player1, new Mountain());

        Permanent giant = findPermanent(player1, "Nightfire Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's Mountain does not grant the boost")
    void opponentMountainDoesNotCount() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.addToBattlefield(player2, new Mountain());

        Permanent giant = findPermanent(player1, "Nightfire Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target player")
    void abilityDamagesPlayer() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature")
    void abilityDamagesCreature() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 5);

        UUID elvesId = findPermanent(player2, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityRequiresMana() {
        harness.addToBattlefield(player1, new NightfireGiant());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
