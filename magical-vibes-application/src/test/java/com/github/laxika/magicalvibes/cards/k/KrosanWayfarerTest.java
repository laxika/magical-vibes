package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrosanWayfarer.class, Forest.class, GrizzlyBears.class})
class KrosanWayfarerTest extends BaseCardTest {

    @Test
    void sacrificingWayfarerIsPaidBeforeAbilityResolves() {
        addReadyWayfarer(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Krosan Wayfarer");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof KrosanWayfarer);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void acceptingMayPutsLandFromHandOntoBattlefieldUntapped() {
        addReadyWayfarer(player1);
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, bears));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void decliningMayLeavesLandInHand() {
        addReadyWayfarer(player1);
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
    }

    private Permanent addReadyWayfarer(Player player) {
        Permanent wayfarer = new Permanent(new KrosanWayfarer());
        wayfarer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wayfarer);
        return wayfarer;
    }
}
