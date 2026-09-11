package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElrondMoonReader.class, GrizzlyBears.class, Forest.class})
class ElrondMoonReaderTest extends BaseCardTest {

    @Test
    @DisplayName("Draws once when you activate a creature's ability and flickers up to two permanents")
    void drawsAndFlickersTargets() {
        Permanent elrond = addCreatureReady(player1, new ElrondMoonReader());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addElrondMana(1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstBear.getId(), secondBear.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(elrond);

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(3)
                .contains(elrond);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void drawsOnlyOnceEachTurn() {
        addCreatureReady(player1, new ElrondMoonReader());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        seedLibrary(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addElrondMana(2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(firstBear.getId()));
        resolveAllTriggers();
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(secondBear.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot target itself or a land")
    void rejectsInvalidTargets() {
        Permanent elrond = addCreatureReady(player1, new ElrondMoonReader());
        Permanent land = addCreatureReady(player1, new Forest());
        addElrondMana(1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(elrond.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void seedLibrary(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }
    }

    private void addElrondMana(int activations) {
        harness.addMana(player1, ManaColor.COLORLESS, 5 * activations);
        harness.addMana(player1, ManaColor.BLUE, 2 * activations);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
