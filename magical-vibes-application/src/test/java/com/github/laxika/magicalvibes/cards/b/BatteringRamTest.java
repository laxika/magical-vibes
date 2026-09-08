package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatteringRam.class, GiantSpider.class, WallOfWood.class})
class BatteringRamTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your combat, Battering Ram gains banding")
    void gainsBandingAtBeginningOfCombat() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ram, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Battering Ram's banding grant expires at end of combat")
    void bandingExpiresAtEndOfCombat() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ram, Keyword.BANDING)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ram, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("When Battering Ram becomes blocked by a Wall, that Wall is scheduled for end-of-combat destruction")
    void becomesBlockedByWallSchedulesDestruction() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());
        ram.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfWood());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && ram.getId().equals(se.getSourcePermanentId())
                        && se.getTargetId().equals(wall.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(wall.getId()));
    }

    @Test
    @DisplayName("A blocking Wall is destroyed at end of combat")
    void blockingWallDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ram = addCreatureReady(player1, new BatteringRam());
        ram.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfWood());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wall);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(wall.getCard());
    }

    @Test
    @DisplayName("When Battering Ram becomes blocked by a non-Wall creature, nothing is scheduled for destruction")
    void becomesBlockedByNonWallSchedulesNothing() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());
        ram.setAttacking(true);
        addCreatureReady(player2, new GiantSpider()); // not a Wall

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("A non-Wall blocker does not create Battering Ram's destruction trigger")
    void nonWallBlockerDoesNotTriggerDestruction() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());
        ram.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && ram.getId().equals(se.getSourcePermanentId())
                        && spider.getId().equals(se.getTargetId()));
    }

    @Test
    @DisplayName("A Wall that loses its Wall subtype after blocking is still destroyed at end of combat")
    void wallConditionIsCheckedWhenItBecomesABlocker() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());
        ram.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfWood());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        TestCards.mutableCard(wall).setSubtypes(List.of());
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(wall.getId()));
    }

    @Test
    @DisplayName("Battering Ram creates one destruction trigger for each blocking Wall")
    void createsOneTriggerPerWallBlocker() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());
        ram.setAttacking(true);
        Permanent firstWall = addCreatureReady(player2, new WallOfWood());
        Permanent secondWall = addCreatureReady(player2, new WallOfWood());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack.stream()
                .filter(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(se -> ram.getId().equals(se.getSourcePermanentId())))
                .hasSize(2);

        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .extracting(DelayedPermanentAction::permanentId)
                .containsExactlyInAnyOrder(firstWall.getId(), secondWall.getId());
    }

}
