package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PyricSalamander;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSeaDevils.class, GrizzlyBears.class, PyricSalamander.class})
class TheSeaDevilsTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates an islandwalking Alien Salamander")
    void chapterOneCreatesSalamander() {
        addSagaWithLore(0);

        triggerNextChapter();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Alien Salamander");
        assertThat(tokens).hasSize(1);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ALIEN, CardSubtype.SALAMANDER);
            assertThat(token.hasKeyword(Keyword.ISLANDWALK)).isTrue();
        });
    }

    @Test
    @DisplayName("Chapter II creates an islandwalking Alien Salamander")
    void chapterTwoCreatesSalamander() {
        addSagaWithLore(1);

        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien Salamander")).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ALIEN, CardSubtype.SALAMANDER);
            assertThat(token.hasKeyword(Keyword.ISLANDWALK)).isTrue();
        });
    }

    @Test
    @DisplayName("Chapter III creates a delayed combat damage trigger")
    void chapterThreeDealsCombatDamageToDamagedPlayersCreature() {
        addCreatureReady(player1, new PyricSalamander());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(2);

        triggerNextChapter();
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III trigger expires at end of turn")
    void chapterThreeTriggerExpiresAtEndOfTurn() {
        addCreatureReady(player1, new PyricSalamander());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(2);

        triggerNextChapter();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chapter III watches Salamanders that enter after it resolves")
    void chapterThreeWatchesLaterSalamanders() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(2);
        triggerNextChapter();
        harness.passBothPriorities();

        addCreatureReady(player1, new PyricSalamander());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Saga controller chooses a target when an opposing Salamander deals damage")
    void chapterThreeWatchesOpposingSalamanders() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new PyricSalamander());
        addSagaWithLore(2);
        triggerNextChapter();
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III requires its creature target before players can respond")
    void chapterThreeChoosesTargetBeforeResolution() {
        addCreatureReady(player1, new PyricSalamander());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(2);
        triggerNextChapter();
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());

        harness.assertLife(player2, 19);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getTargetId()).isEqualTo(target.getId());
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });
        resolveAllTriggers();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheSeaDevils());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
