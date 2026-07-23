package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InfuseTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target creature and schedules a draw at the next upkeep")
    void untapsCreatureAndSchedulesDraw() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(giantId)).findFirst().orElseThrow();
        giant.tap();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        assertThat(giant.isTapped()).isFalse();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can untap a target land")
    void untapsLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        Permanent forest = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(forestId)).findFirst().orElseThrow();
        forest.tap();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, forestId);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }
}
