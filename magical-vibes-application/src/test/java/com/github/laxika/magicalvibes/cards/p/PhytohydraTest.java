package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Phytohydra.class, GrizzlyBears.class, Humility.class, LeylineOfPunishment.class, Shock.class})
class PhytohydraTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage is replaced with +1/+1 counters")
    void noncombatDamageReplacedWithCounters() {
        harness.addToBattlefield(player2, new Phytohydra());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID phytohydraId = harness.getPermanentId(player2, "Phytohydra");
        harness.castInstant(player1, 0, phytohydraId);
        harness.passBothPriorities();

        Permanent phytohydra = findPermanent(player2, "Phytohydra");
        assertThat(phytohydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(phytohydra.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage that cannot be prevented is still replaced with counters")
    void damageThatCannotBePreventedIsReplaced() {
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        harness.addToBattlefield(player2, new Phytohydra());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID phytohydraId = harness.getPermanentId(player2, "Phytohydra");
        harness.castInstant(player1, 0, phytohydraId);
        harness.passBothPriorities();

        Permanent phytohydra = findPermanent(player2, "Phytohydra");
        assertThat(phytohydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(phytohydra.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage is not replaced after Phytohydra loses its ability")
    void damageIsNotReplacedAfterLosingAbility() {
        harness.addToBattlefield(player1, new Humility());
        harness.addToBattlefield(player2, new Phytohydra());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID phytohydraId = harness.getPermanentId(player2, "Phytohydra");
        harness.castInstant(player1, 0, phytohydraId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Phytohydra"));
    }

    @Test
    @DisplayName("Combat damage is replaced with +1/+1 counters")
    void combatDamageReplacedWithCounters() {
        Permanent blocker = addCreatureReady(player2, new Phytohydra());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Phytohydra");
        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }
}
