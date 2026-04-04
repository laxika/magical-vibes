package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TumbleMagnet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GremlinMineTest extends BaseCardTest {

    @Test
    @DisplayName("First ability deals 4 damage to target artifact creature")
    void dealsDamageToArtifactCreature() {
        harness.addToBattlefield(player1, new GremlinMine());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Iron Myr");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iron Myr");
        harness.assertInGraveyard(player2, "Iron Myr");
    }

    @Test
    @DisplayName("Gremlin Mine is sacrificed when activating first ability")
    void sacrificedOnFirstAbility() {
        harness.addToBattlefield(player1, new GremlinMine());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Iron Myr");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Gremlin Mine");
        harness.assertInGraveyard(player1, "Gremlin Mine");
    }

    @Test
    @DisplayName("First ability cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new GremlinMine());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }

    @Test
    @DisplayName("Second ability removes charge counters from target noncreature artifact")
    void removesChargeCounters() {
        harness.addToBattlefield(player1, new GremlinMine());
        harness.addToBattlefield(player2, new TumbleMagnet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // TumbleMagnet enters with 3 charge counters via EnterWithFixedChargeCountersEffect
        // but addToBattlefield doesn't trigger ETB, so set manually
        Permanent magnet = findPermanent(player2, "Tumble Magnet");
        magnet.setChargeCounters(3);

        harness.activateAbility(player1, 0, 1, null, magnet.getId());
        harness.passBothPriorities();

        assertThat(magnet.getChargeCounters()).isZero();
    }

    @Test
    @DisplayName("Second ability removes at most 4 charge counters when target has more")
    void removesAtMostFourChargeCounters() {
        harness.addToBattlefield(player1, new GremlinMine());
        harness.addToBattlefield(player2, new TumbleMagnet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent magnet = findPermanent(player2, "Tumble Magnet");
        magnet.setChargeCounters(6);

        harness.activateAbility(player1, 0, 1, null, magnet.getId());
        harness.passBothPriorities();

        assertThat(magnet.getChargeCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability cannot target an artifact creature")
    void secondAbilityCannotTargetArtifactCreature() {
        harness.addToBattlefield(player1, new GremlinMine());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Iron Myr");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new GremlinMine());
        harness.addToBattlefield(player2, new IronMyr());

        Permanent target = findPermanent(player2, "Iron Myr");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
