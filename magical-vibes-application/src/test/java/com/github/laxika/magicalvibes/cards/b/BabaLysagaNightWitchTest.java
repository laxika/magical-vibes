package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BabaLysagaNightWitch.class, Forest.class, GrizzlyBears.class, SolRing.class, Ornithopter.class})
class BabaLysagaNightWitchTest extends BaseCardTest {

    @Test
    void rewardsSacrificingThreeDistinctPermanentTypes() {
        Permanent baba = addCreatureReady(player1, new BabaLysagaNightWitch());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), land.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore + 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(baba);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Sol Ring");
    }

    @Test
    void doesNothingWithFewerThanThreeDistinctTypes() {
        addCreatureReady(player1, new BabaLysagaNightWitch());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void cannotSacrificeMoreThanThreePermanents() {
        addCreatureReady(player1, new BabaLysagaNightWitch());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new SolRing());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    void twoPermanentsWithThreeCardTypesReceiveTheReward() {
        Permanent baba = addCreatureReady(player1, new BabaLysagaNightWitch());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultiplePermanentsChosen(player1, List.of(artifactCreature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(baba.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void canSacrificeItselfAndStillResolveTheReward() {
        Permanent baba = addCreatureReady(player1, new BabaLysagaNightWitch());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultiplePermanentsChosen(player1, List.of(baba.getId(), land.getId(), artifact.getId()));
        harness.assertInGraveyard(player1, "Baba Lysaga, Night Witch");
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        harness.assertNotOnBattlefield(player1, "Baba Lysaga, Night Witch");
    }

    @Test
    void canSacrificeZeroPermanentsWithoutReceivingTheReward() {
        Permanent baba = addCreatureReady(player1, new BabaLysagaNightWitch());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(baba);
        assertThat(baba.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent baba = harness.addToBattlefieldAndReturn(player1, new BabaLysagaNightWitch());
        baba.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(baba.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
