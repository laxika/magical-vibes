package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BabaLysagaNightWitch.class, Forest.class, GrizzlyBears.class, SolRing.class})
class BabaLysagaNightWitchTest extends BaseCardTest {

    @Test
    void rewardsSacrificingThreeDistinctPermanentTypes() {
        Permanent baba = harness.addToBattlefieldAndReturn(player1, new BabaLysagaNightWitch());
        baba.setSummoningSick(false);
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
        Permanent baba = harness.addToBattlefieldAndReturn(player1, new BabaLysagaNightWitch());
        baba.setSummoningSick(false);
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
        Permanent baba = harness.addToBattlefieldAndReturn(player1, new BabaLysagaNightWitch());
        baba.setSummoningSick(false);
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
}
