package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DeathcurseOgre;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuganTheRisingStar.class, RendSpirit.class, DeathcurseOgre.class, HumbleBudoka.class})
class JuganTheRisingStarTest extends BaseCardTest {

    @Test
    @DisplayName("On death, distributes five +1/+1 counters among the chosen creatures")
    void deathDistributesFiveCounters() {
        Permanent jugan = addCreatureReady(player1, new JuganTheRisingStar());
        jugan.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());

        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 3, giant.getId(), 2);

        killJugan(jugan);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters may go on creatures an opponent controls")
    void deathDistributesToOpponentCreatures() {
        Permanent jugan = addCreatureReady(player1, new JuganTheRisingStar());
        jugan.tap();
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        Permanent oppGiant = harness.addToBattlefieldAndReturn(player2, new DeathcurseOgre());

        gd.pendingETBDamageAssignments = Map.of(ownBears.getId(), 1, oppGiant.getId(), 4);

        killJugan(jugan);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(oppGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(oppGiant.getEffectivePower()).isEqualTo(7);
    }

    @Test
    @DisplayName("The distribution is optional — declining places no counters")
    void deathDistributeCanBeDeclined() {
        Permanent jugan = addCreatureReady(player1, new JuganTheRisingStar());
        jugan.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());

        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 5);

        killJugan(jugan);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Accepting with no targets places no counters")
    void deathDistributeCanChooseNoTargets() {
        Permanent jugan = addCreatureReady(player1, new JuganTheRisingStar());
        jugan.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());

        gd.pendingETBDamageAssignments = Map.of();

        killJugan(jugan);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Shrouded creatures are not legal targets")
    void deathDistributeDoesNotAffectShroudedCreature() {
        Permanent jugan = addCreatureReady(player1, new JuganTheRisingStar());
        jugan.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DeathcurseOgre());
        Permanent shroudedBudoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());

        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 3, shroudedBudoka.getId(), 2);

        killJugan(jugan);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(shroudedBudoka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void killJugan(Permanent jugan) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID juganId = jugan.getId();
        gs.playCard(gd, player1, 0, 0, juganId, null);
        harness.passBothPriorities(); // Rend Spirit resolves → Jugan dies → death trigger on stack
    }
}
