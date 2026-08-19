package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrenchingSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land gives Trenching Steed +0/+3 until end of turn")
    void sacrificeLandBoostsSelf() {
        Permanent steed = addCreatureReady(player1, new TrenchingSteed());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(steed.getPowerModifier()).isEqualTo(0);
        assertThat(steed.getToughnessModifier()).isEqualTo(3);
        assertThat(landsOnBattlefield()).isEmpty();
    }

    @Test
    @DisplayName("With multiple lands, prompts which land to sacrifice")
    void multipleLandsPromptChoice() {
        addCreatureReady(player1, new TrenchingSteed());
        Permanent forestA = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, forestA.getId());
        harness.passBothPriorities();

        assertThat(landsOnBattlefield()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void cannotActivateWithoutLand() {
        addCreatureReady(player1, new TrenchingSteed());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }

    @Test
    @DisplayName("The toughness boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent steed = addCreatureReady(player1, new TrenchingSteed());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(steed.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(steed.getPowerModifier()).isEqualTo(0);
        assertThat(steed.getToughnessModifier()).isEqualTo(0);
    }

    private java.util.List<Permanent> landsOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
    }
}
