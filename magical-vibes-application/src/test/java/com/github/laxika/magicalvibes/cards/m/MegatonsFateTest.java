package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MegatonsFate.class, ZuranOrb.class, GrizzlyBears.class})
class MegatonsFateTest extends BaseCardTest {

    @Test
    void disarmDestroysAnArtifactAndCreatesFourTreasures() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        harness.setHand(player1, List.of(new MegatonsFate()));
        addManaForMegatonsFate();

        harness.castAndResolveSorcery(player1, 0, 0, artifact.getId());

        harness.assertInGraveyard(player2, "Zuran Orb");
        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
    }

    @Test
    void detonateDealsEightDamageToEachCreatureAndGivesEachPlayerFourRadCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MegatonsFate()));
        addManaForMegatonsFate();

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void disarmCannotTargetANonartifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MegatonsFate()));
        addManaForMegatonsFate();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void disarmCreatesNoTreasuresWhenItsOnlyTargetLeavesTheBattlefield() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        harness.setHand(player1, List.of(new MegatonsFate()));
        addManaForMegatonsFate();

        harness.castSorcery(player1, 0, 0, artifact.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, artifact));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Megaton's Fate");
    }

    @Test
    void detonateAddsRadCountersWithoutCreaturesAndDoesNotDamagePlayersOrArtifacts() {
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerRadCounters.put(player1.getId(), 2);
        gd.playerRadCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new MegatonsFate()));
        addManaForMegatonsFate();

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player2, "Zuran Orb");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(6);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(7);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void addManaForMegatonsFate() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
