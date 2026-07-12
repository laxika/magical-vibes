package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoastalHornclawTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land grants flying until end of turn")
    void sacrificeLandGrantsFlying() {
        Permanent hornclaw = addCreatureReady(player1, new CoastalHornclaw());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hornclaw.getGrantedKeywords()).contains(Keyword.FLYING);
        // The land was sacrificed.
        assertThat(landsOnBattlefield()).isEmpty();
    }

    @Test
    @DisplayName("With multiple lands, prompts which to sacrifice")
    void multipleLandsPromptsChoice() {
        Permanent hornclaw = addCreatureReady(player1, new CoastalHornclaw());
        Permanent forestA = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, forestA.getId());
        harness.passBothPriorities();

        assertThat(hornclaw.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(landsOnBattlefield()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void cannotActivateWithoutLand() {
        addCreatureReady(player1, new CoastalHornclaw());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent hornclaw = addCreatureReady(player1, new CoastalHornclaw());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(hornclaw.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hornclaw.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    private java.util.List<Permanent> landsOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
    }
}
