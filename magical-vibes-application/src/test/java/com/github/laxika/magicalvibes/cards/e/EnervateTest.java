package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

class EnervateTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature and schedules a draw at the next upkeep")
    void tapsCreatureAndSchedulesDraw() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.isTapped()).isTrue();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can tap a target land")
    void tapsLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.castInstant(player1, 0, forestId);
        harness.passBothPriorities();

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a noncreature artifact")
    void tapsArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, fountainId);
        harness.passBothPriorities();

        Permanent fountain = findPermanent(player2, "Fountain of Youth");
        assertThat(fountain.isTapped()).isTrue();
    }
}
