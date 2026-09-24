package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerdantConfluence.class, Forest.class, GhostlyPrison.class, GrizzlyBears.class})
class VerdantConfluenceTest extends BaseCardTest {

    @Test
    void resolvesAllThreeModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GhostlyPrison returned = new GhostlyPrison();
        Forest searched = new Forest();
        harness.setGraveyard(player1, List.of(returned));
        harness.setLibrary(player1, List.of(searched));

        cast(new int[]{0, 1, 2}, List.of(creature.getId(), returned.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player1, "Ghostly Prison");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(searched.getId()) && permanent.isTapped());
    }

    @Test
    void repeatedCounterModeCanTargetTheSameCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(new int[]{0, 0, 0}, List.of(creature.getId(), creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void counterModeRejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> cast(new int[]{0, 0, 0}, List.of(land.getId(), land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new VerdantConfluence()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices),
                null, null, targets, List.of());
    }
}
