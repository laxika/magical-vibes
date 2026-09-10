package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PlagueBeetle;
import com.github.laxika.magicalvibes.cards.s.Swat;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkwatchElves.class, PlagueBeetle.class, Swat.class})
class DarkwatchElvesTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from black")
    void hasProtectionFromBlack() {
        Permanent elves = addCreatureReady(player1, new DarkwatchElves());

        assertThat(gqs.hasProtectionFrom(gd, elves, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, elves, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent elves = addCreatureReady(player1, new DarkwatchElves());
        harness.setHand(player2, List.of(new Swat()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Cannot be blocked by a black creature")
    void cannotBeBlockedByBlackCreature() {
        Permanent elves = addCreatureReady(player1, new DarkwatchElves());
        elves.setAttacking(true);
        addCreatureReady(player2, new PlagueBeetle());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Prevents combat damage from a black creature")
    void preventsCombatDamageFromBlackCreature() {
        Permanent attacker = addCreatureReady(player2, new PlagueBeetle());
        attacker.setAttacking(true);
        Permanent elves = addCreatureReady(player1, new DarkwatchElves());
        elves.setBlocking(true);
        elves.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(elves.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DarkwatchElves()));
        harness.setLibrary(player1, List.of(new Swat()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Darkwatch Elves");
        harness.assertInHand(player1, "Swat");
    }
}
