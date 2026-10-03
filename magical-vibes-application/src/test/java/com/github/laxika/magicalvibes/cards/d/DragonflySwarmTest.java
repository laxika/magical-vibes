package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonflySwarm.class, AirbendingLesson.class, LightningStrike.class, Plains.class})
class DragonflySwarmTest extends BaseCardTest {

    @Test
    void powerCountsOwnNoncreatureNonlandCardsInGraveyard() {
        Permanent swarm = harness.addToBattlefieldAndReturn(player1, new DragonflySwarm());
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new LightningStrike(), new DragonflySwarm(), new Plains()));
        harness.setGraveyard(player2, List.of(new LightningStrike()));

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(3);
    }

    @Test
    void powerUpdatesAsMatchingCardsEnterOwnGraveyard() {
        Permanent swarm = harness.addToBattlefieldAndReturn(player1, new DragonflySwarm());
        harness.setGraveyard(player1, List.of(new LightningStrike()));

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new AirbendingLesson());

        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(2);
    }

    @Test
    void deathTriggerDrawsWithLessonInControllerGraveyard() {
        harness.addToBattlefield(player1, new DragonflySwarm());
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        Card drawn = new DragonflySwarm();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Dragonfly Swarm"));
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Dragonfly Swarm");
    }

    @Test
    void deathTriggerDoesNotDrawWithoutLessonInControllerGraveyard() {
        harness.addToBattlefield(player1, new DragonflySwarm());
        Card drawn = new DragonflySwarm();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Dragonfly Swarm"));
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dragonfly Swarm");
    }

    @Test
    void powerReturnsToZeroWhenMatchingCardsLeaveGraveyard() {
        Permanent swarm = harness.addToBattlefieldAndReturn(player1, new DragonflySwarm());
        harness.setGraveyard(player1, List.of(new LightningStrike()));
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new DragonflySwarm(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, swarm)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(3);
    }

    @Test
    void unpaidWardCountersLightningStrikeAndPreventsDeath() {
        harness.addToBattlefield(player1, new DragonflySwarm());
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Dragonfly Swarm"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dragonfly Swarm");
        harness.assertInGraveyard(player2, "Lightning Strike");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsLessonDoesNotEnableDeathTrigger() {
        harness.addToBattlefield(player1, new DragonflySwarm());
        harness.setGraveyard(player2, List.of(new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Dragonfly Swarm"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dragonfly Swarm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingLastLessonBeforeDeathTriggerResolvesPreventsDraw() {
        harness.addToBattlefield(player1, new DragonflySwarm());
        Card lesson = new AirbendingLesson();
        harness.setGraveyard(player1, List.of(lesson));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Dragonfly Swarm"));
        assertThat(gd.stack).hasSize(1);
        gd.playerGraveyards.get(player1.getId()).remove(lesson);
        harness.setExile(player1, List.of(lesson));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dragonfly Swarm");
    }

    @Test
    void addingLessonAfterDeathDoesNotCreateTrigger() {
        harness.addToBattlefield(player1, new DragonflySwarm());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Dragonfly Swarm"));
        assertThat(gd.stack).isEmpty();
        gd.playerGraveyards.get(player1.getId()).add(new AirbendingLesson());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dragonfly Swarm");
    }
}
