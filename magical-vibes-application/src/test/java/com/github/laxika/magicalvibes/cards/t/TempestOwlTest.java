package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TempestOwlTest extends BaseCardTest {

    @Test
    void kickedEtbTapsUpToThreeTargetPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());

        castKicked(List.of(creature.getId(), artifact.getId(), enchantment.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
        assertThat(enchantment.isTapped()).isTrue();
    }

    @Test
    void nonKickedEtbDoesNotTapTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TempestOwl()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    private void castKicked(List<java.util.UUID> targetIds) {
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TempestOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null, targetIds, List.of(), false,
                null, null, null, null, null, true);
        resolveAllTriggers();
    }
}
