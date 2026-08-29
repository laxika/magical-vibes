package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlameshotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage divided among one, two, or three target creatures")
    void dividesDamageAmongThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Flameshot()));
        addManaForManaCost();

        harness.castSorcery(player1, 0, Map.of(
                first.getId(), 1,
                second.getId(), 1,
                third.getId(), 1
        ));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can be cast by discarding a Mountain")
    void canBeCastByDiscardingMountain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Flameshot(), new Mountain()));

        castWithDiscard(Map.of(target.getId(), 3), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flameshot");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Alternate cost requires discarding a Mountain")
    void alternateCostRequiresMountain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Flameshot(), new LlanowarElves()));

        assertThatThrownBy(() -> castWithDiscard(Map.of(target.getId(), 3), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Flameshot()));
        addManaForManaCost();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustSumToThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Flameshot()));
        addManaForManaCost();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForManaCost() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castWithDiscard(Map<UUID, Integer> damageAssignments, int discardHandCardIndex) {
        gs.playCard(gd, player1, 0, 0, null, damageAssignments, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, discardHandCardIndex);
    }
}
