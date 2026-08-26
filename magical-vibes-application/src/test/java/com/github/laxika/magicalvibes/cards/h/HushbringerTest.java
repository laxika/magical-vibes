package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Bloodbriar;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lumberknot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hushbringer.class, Bloodbriar.class, CruelEdict.class, GrizzlyBears.class,
        Lumberknot.class, Shock.class, SoulWarden.class})
class HushbringerTest extends BaseCardTest {

    @Test
    void suppressesCreatureEnteringTriggers() {
        harness.addToBattlefield(player1, new Hushbringer());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void suppressesCreatureDeathTriggers() {
        harness.addToBattlefield(player1, new Hushbringer());
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player1, new Lumberknot());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotSuppressSacrificeTriggers() {
        harness.addToBattlefield(player1, new Hushbringer());
        Permanent bloodbriar = harness.addToBattlefieldAndReturn(player1, new Bloodbriar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
