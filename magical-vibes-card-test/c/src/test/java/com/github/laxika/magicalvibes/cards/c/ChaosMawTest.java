package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaosMaw.class, GrizzlyBears.class, AirElemental.class, Unsummon.class, Cloudshift.class})
class ChaosMawTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 3 damage to opponent's 2/2 creature, killing it")
    void etbKillsOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castChaosMaw();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB deals 3 damage to controller's own 2/2 creature, killing it")
    void etbKillsControllerCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castChaosMaw();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB marks 3 damage on a surviving creature but not on itself")
    void etbDamagesOthersNotSelf() {
        harness.addToBattlefield(player2, new AirElemental()); // 4/4 survives 3 damage

        castChaosMaw();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isEqualTo(3);

        // Chaos Maw itself survives with no marked damage
        assertThat(findPermanent(player1, "Chaos Maw").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("ETB does not deal damage to players")
    void etbDoesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castChaosMaw();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB still damages other creatures after Chaos Maw leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castChaosMaw();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Chaos Maw"));
        harness.assertInHand(player1, "Chaos Maw");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An older ETB trigger damages Chaos Maw after it is blinked")
    void olderTriggerDamagesReturnedChaosMaw() {
        castChaosMaw();
        harness.passBothPriorities();
        var originalId = harness.getPermanentId(player1, "Chaos Maw");

        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, originalId);
        assertThat(harness.getPermanentId(player1, "Chaos Maw")).isNotEqualTo(originalId);

        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Chaos Maw").getMarkedDamage()).isZero();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Chaos Maw").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB damages another Chaos Maw while excluding only its own source")
    void triggerDamagesAnotherChaosMaw() {
        harness.addToBattlefield(player2, new ChaosMaw());
        castChaosMaw();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Chaos Maw").getMarkedDamage()).isEqualTo(3);
        assertThat(findPermanent(player1, "Chaos Maw").getMarkedDamage()).isZero();
    }

    private void castChaosMaw() {
        harness.castFromHand(player1, new ChaosMaw(), "{5}{R}{R}");
    }
}
