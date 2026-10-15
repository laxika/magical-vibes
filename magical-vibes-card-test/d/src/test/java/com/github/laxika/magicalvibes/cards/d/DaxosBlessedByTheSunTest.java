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

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
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

    @Test
    @DisplayName("Toughness decreases immediately when a white permanent dies")
    void toughnessDecreasesWhenWhitePermanentDies() {
        Permanent daxos = addCreatureReady(player1, new DaxosBlessedByTheSun());
        addCreatureReady(player1, new SavannahLions());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gqs.getEffectiveToughness(gd, daxos)).isEqualTo(3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Savannah Lions"));

        assertThat(gqs.getEffectiveToughness(gd, daxos)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life when Daxos himself dies")
    void excludesOwnDeath() {
        addCreatureReady(player1, new DaxosBlessedByTheSun());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Daxos, Blessed by the Sun"));

        harness.assertInGraveyard(player1, "Daxos, Blessed by the Sun");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not gain life when an opponent's creature dies")
    void excludesOpponentsDeath() {
        addCreatureReady(player1, new DaxosBlessedByTheSun());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
