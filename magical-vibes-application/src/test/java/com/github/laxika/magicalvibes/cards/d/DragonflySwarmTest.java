package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonflySwarm.class, AirbendingLesson.class, GrizzlyBears.class, LightningStrike.class,
        Plains.class, Shock.class})
class DragonflySwarmTest extends BaseCardTest {

    @Test
    void powerCountsOwnNoncreatureNonlandCardsInGraveyard() {
        Permanent swarm = harness.addToBattlefieldAndReturn(player1, new DragonflySwarm());
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new Shock(), new GrizzlyBears(), new Plains()));
        harness.setGraveyard(player2, List.of(new Shock()));

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(3);
    }

    @Test
    void powerUpdatesAsMatchingCardsEnterOwnGraveyard() {
        Permanent swarm = harness.addToBattlefieldAndReturn(player1, new DragonflySwarm());
        harness.setGraveyard(player1, List.of(new Shock()));

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new AirbendingLesson());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(2);
    }

    @Test
    void deathTriggerDrawsWithLessonInControllerGraveyard() {
        harness.addToBattlefield(player1, new DragonflySwarm());
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, gd.playerBattlefields.get(player1.getId()).get(0).getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Dragonfly Swarm");
    }

    @Test
    void deathTriggerDoesNotDrawWithoutLessonInControllerGraveyard() {
        harness.addToBattlefield(player1, new DragonflySwarm());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, gd.playerBattlefields.get(player1.getId()).get(0).getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dragonfly Swarm");
    }
}
