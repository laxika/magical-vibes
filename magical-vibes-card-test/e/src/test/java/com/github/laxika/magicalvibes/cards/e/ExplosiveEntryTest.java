package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AkkiRonin;
import com.github.laxika.magicalvibes.cards.b.BronzeplateBoar;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExplosiveEntry.class, AkkiRonin.class, NetworkTerminal.class, BronzeplateBoar.class})
class ExplosiveEntryTest extends BaseCardTest {

    @Test
    void destroysArtifactAndPutsCounterOnCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AkkiRonin());
        cast(List.of(artifact.getId(), creature.getId()));

        harness.assertInGraveyard(player2, "Network Terminal");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayChooseNoTargets() {
        cast(List.of());

        harness.assertInGraveyard(player1, "Explosive Entry");
    }

    @Test
    void mayChooseOnlyACreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AkkiRonin());
        cast(List.of(creature.getId()));

        harness.assertOnBattlefield(player2, "Akki Ronin");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayChooseOnlyAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AkkiRonin());
        cast(List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Network Terminal");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayChooseTheSameArtifactCreatureForBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BronzeplateBoar());
        cast(List.of(creature.getId(), creature.getId()));

        harness.assertInGraveyard(player2, "Bronzeplate Boar");
        harness.assertNotOnBattlefield(player2, "Bronzeplate Boar");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillPutsCounterWhenArtifactTargetLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AkkiRonin());
        prepareSpell();
        harness.castSorcery(player1, 0, List.of(artifact.getId(), creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Explosive Entry");
    }

    @Test
    void stillDestroysArtifactWhenCreatureTargetLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AkkiRonin());
        prepareSpell();
        harness.castSorcery(player1, 0, List.of(artifact.getId(), creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Network Terminal");
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void cast(List<UUID> targetIds) {
        prepareSpell();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ExplosiveEntry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
