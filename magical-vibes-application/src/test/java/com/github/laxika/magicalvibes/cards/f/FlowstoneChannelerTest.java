package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FlowstoneChanneler.class, AshcoatBear.class, Forest.class})
class FlowstoneChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and activating the ability gives a creature +1/-1 and haste")
    void boostsTargetCreatureAndGivesHaste() {
        Permanent channeler = addCreatureReady(player1, new FlowstoneChanneler());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(channeler.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("The boost and haste wear off at cleanup")
    void effectWearsOffAtCleanup() {
        addCreatureReady(player1, new FlowstoneChanneler());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new FlowstoneChanneler());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent channeler = addCreatureReady(player1, new FlowstoneChanneler());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(channeler.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
    @Test
    @DisplayName("Can target itself and discard a land as the activation cost")
    void canTargetItselfAndDiscardLand() {
        Permanent channeler = addCreatureReady(player1, new FlowstoneChanneler());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, channeler.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(channeler.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(channeler.getPowerModifier()).isZero();
        assertThat(channeler.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(channeler.getPowerModifier()).isEqualTo(1);
        assertThat(channeler.getToughnessModifier()).isEqualTo(-1);
        assertThat(channeler.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new FlowstoneChanneler());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(channeler.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Two activations stack and put a two-toughness creature into the graveyard")
    void repeatedActivationsReduceToughnessToZero() {
        addCreatureReady(player1, new FlowstoneChanneler());
        addCreatureReady(player1, new FlowstoneChanneler());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Ashcoat Bear");

        harness.activateAbility(player1, 1, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Ashcoat Bear");
    }
}
