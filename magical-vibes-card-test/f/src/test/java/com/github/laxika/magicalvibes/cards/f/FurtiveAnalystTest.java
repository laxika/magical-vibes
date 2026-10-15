package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FurtiveAnalyst.class, Forest.class})
class FurtiveAnalystTest extends BaseCardTest {

    @Test
    @DisplayName("The ability draws a card, then discards a card")
    void drawsThenDiscards() {
        Permanent analyst = addCreatureReady(player1, new FurtiveAnalyst());
        harness.setHand(player1, List.of(new FurtiveAnalyst()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().extracting(Object::getClass)
                .isEqualTo(Forest.class);
        harness.assertInGraveyard(player1, "Furtive Analyst");
        assertThat(analyst.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The drawn card can be chosen for discard")
    void canDiscardDrawnCard() {
        addCreatureReady(player1, new FurtiveAnalyst());
        harness.setHand(player1, List.of(new FurtiveAnalyst()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Furtive Analyst");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty hand still draws and then discards the drawn card")
    void canActivateWithEmptyHand() {
        addCreatureReady(player1, new FurtiveAnalyst());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation pays the tap cost before drawing")
    void activationTapsBeforeResolution() {
        Permanent analyst = addCreatureReady(player1, new FurtiveAnalyst());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(analyst.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activation requires two mana")
    void cannotActivateWithOnlyOneMana() {
        Permanent analyst = addCreatureReady(player1, new FurtiveAnalyst());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(analyst.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Analyst cannot activate the ability")
    void cannotActivateWhenTapped() {
        Permanent analyst = addCreatureReady(player1, new FurtiveAnalyst());
        analyst.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents activation")
    void cannotActivateWithSummoningSickness() {
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new FurtiveAnalyst());
        analyst.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(analyst.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("Attacking does not tap Furtive Analyst")
    void attackingDoesNotTap() {
        Permanent analyst = addCreatureReady(player1, new FurtiveAnalyst());
        addCreatureReady(player2, new FurtiveAnalyst());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(analyst.isAttacking()).isTrue();
        assertThat(analyst.isTapped()).isFalse();
    }
}
