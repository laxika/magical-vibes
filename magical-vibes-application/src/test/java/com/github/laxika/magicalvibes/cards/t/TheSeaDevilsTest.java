package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PyricSalamander;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SALAMANDER);
            assertThat(token.hasKeyword(Keyword.ISLANDWALK)).isTrue();
        });
    }

    @Test
    @DisplayName("Chapter II creates an islandwalking Alien Salamander")
    void chapterTwoCreatesSalamander() {
        addSagaWithLore(1);

        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Alien Salamander")).hasSize(1);
    }

    @Test
    @DisplayName("Chapter III gives Salamanders a temporary damage trigger")
    void chapterThreeDealsCombatDamageToDamagedPlayersCreature() {
        Permanent salamander = addCreatureReady(player1, new PyricSalamander());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(2);

        triggerNextChapter();
        harness.passBothPriorities();
        assertThat(salamander.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).isNotEmpty();

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III trigger expires at end of turn")
    void chapterThreeTriggerExpiresAtEndOfTurn() {
        Permanent salamander = addCreatureReady(player1, new PyricSalamander());
        addSagaWithLore(2);

        triggerNextChapter();
        harness.passBothPriorities();
        assertThat(salamander.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).isNotEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(salamander.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).isEmpty();
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
