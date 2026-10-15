package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ApothecaryGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SupremePhantom.class, ApothecaryGeist.class, GrizzlyBears.class})
class SupremePhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Other Spirits you control get +1/+1")
    void buffsOtherOwnSpirits() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new ApothecaryGeist());
        harness.addToBattlefield(player1, new SupremePhantom());

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(4);
    }

    @Test
    @DisplayName("Supreme Phantom does not buff itself")
    void doesNotBuffItself() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new SupremePhantom());
        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, phantom)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Spirit creatures")
    void doesNotBuffNonSpirits() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SupremePhantom());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Spirits")
    void doesNotBuffOpponentSpirits() {
        harness.addToBattlefield(player1, new SupremePhantom());
        Permanent geist = harness.addToBattlefieldAndReturn(player2, new ApothecaryGeist());
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Supreme Phantoms buff each other")
    void twoPhantomsBuffEachOther() {
        harness.addToBattlefield(player1, new SupremePhantom());
        harness.addToBattlefield(player1, new SupremePhantom());

        assertThat(findPermanents(player1, "Supreme Phantom").stream()
                .map(p -> gqs.getEffectivePower(gd, p)))
                .containsExactly(2, 2);
    }

    @Test
    @DisplayName("Buff wears off when Supreme Phantom leaves the battlefield")
    void buffEndsWhenPhantomLeaves() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new ApothecaryGeist());
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new SupremePhantom());

        gd.playerBattlefields.get(player1.getId()).remove(phantom);

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Spirit entering later receives all existing boosts immediately")
    void laterSpiritReceivesStackedBoosts() {
        harness.addToBattlefield(player1, new SupremePhantom());
        harness.addToBattlefield(player1, new SupremePhantom());

        harness.castFromHand(player1, new SupremePhantom(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Supreme Phantom")).hasSize(3).allSatisfy(phantom -> {
            assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, phantom)).isEqualTo(5);
        });
    }

    @Test
    @DisplayName("Losing a boost makes previously nonlethal damage lethal")
    void losingBoostKillsDamagedSpirit() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SupremePhantom());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SupremePhantom());
        second.addMarkedDamage(null, 3);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);

        first.addMarkedDamage(null, 4);
        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Supreme Phantom")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first.getCard(), second.getCard());
    }
}
