package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaxosBlessedByTheSun.class, GrizzlyBears.class, SavannahLions.class, Shock.class})
class DaxosBlessedByTheSunTest extends BaseCardTest {

    @Test
    @DisplayName("Toughness equals white devotion while power remains 2")
    void toughnessEqualsWhiteDevotion() {
        Permanent daxos = addCreatureReady(player1, new DaxosBlessedByTheSun());

        assertThat(gqs.getEffectivePower(gd, daxos)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, daxos)).isEqualTo(2);

        addCreatureReady(player1, new SavannahLions());
        addCreatureReady(player2, new SavannahLions());

        assertThat(gqs.getEffectivePower(gd, daxos)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, daxos)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new DaxosBlessedByTheSun());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Gains 1 life when another creature you control dies")
    void gainsLifeOnAllyCreatureDeath() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new DaxosBlessedByTheSun());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger for its own entry or an opponent's creature")
    void excludesOwnAndOpponentsEntries() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DaxosBlessedByTheSun()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
