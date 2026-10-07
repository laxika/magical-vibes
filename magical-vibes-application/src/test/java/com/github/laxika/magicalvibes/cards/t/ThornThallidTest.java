package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Aeolipile;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornThallid.class, Thallid.class, Aeolipile.class, ChandraNalaar.class})
class ThornThallidTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during an opponent's upkeep")
    void upkeepTriggerOnlyFiresDuringControllerUpkeep() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player2);

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Removing three spore counters deals 1 damage to a target creature")
    void removesThreeSporeCountersAndDealsDamage() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new Thallid());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three spore counters leaves any additional spore counters")
    void removesExactlyThreeSporeCounters() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 4);
        Permanent target = addCreatureReady(player2, new Thallid());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isOne();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three spore counters deals 1 damage to a target player")
    void removesThreeSporeCountersAndDealsDamageToPlayer() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Removing three spore counters deals 1 damage to a target planeswalker")
    void removesThreeSporeCountersAndDealsDamageToPlaneswalker() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("The damage ability requires three spore counters")
    void damageAbilityRequiresThreeSporeCounters() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The damage ability does not require the source to be untapped")
    void damageAbilityDoesNotRequireUntappedSource() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        thallid.tap();
        Permanent target = addCreatureReady(player2, new Thallid());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(thallid.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The damage ability cannot target a noncreature permanent")
    void damageAbilityCannotTargetArtifact() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Aeolipile());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Spore counters are paid immediately and cannot pay for a second activation")
    void countersArePaidBeforeResolution() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A summoning-sick Thorn Thallid can damage itself")
    void summoningSickSourceCanTargetItself() {
        Permanent thallid = harness.addToBattlefieldAndReturn(player1, new ThornThallid());
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, thallid.getId());
        harness.passBothPriorities();

        assertThat(thallid.getMarkedDamage()).isEqualTo(1);
        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        harness.assertOnBattlefield(player1, "Thorn Thallid");
    }

    @Test
    @DisplayName("Six spore counters can pay for two independent activations")
    void canActivateTwiceBeforeEitherAbilityResolves() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 6);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The damage ability resolves even after Thorn Thallid dies")
    void damageResolvesAfterSourceDies() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.addToBattlefield(player2, new Aeolipile());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player2, 0, null, thallid.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thorn Thallid");
        harness.assertInGraveyard(player1, "Thorn Thallid");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addThallid() {
        return addCreatureReady(player1, new ThornThallid());
    }
}
