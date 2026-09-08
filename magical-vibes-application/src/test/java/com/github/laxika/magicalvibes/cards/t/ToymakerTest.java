package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToymakerTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card and animates a noncreature artifact with P/T equal to its mana value")
    void animatesArtifactWithManaValuePt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new Toymaker());
        harness.addToBattlefield(player1, new Millstone());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent millstone = findPermanent(player1, "Millstone");
        harness.activateAbility(player1, 0, 0, null, millstone.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        millstone = findPermanent(player1, "Millstone");
        assertThat(millstone.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(millstone.getEffectivePower()).isEqualTo(2);
        assertThat(millstone.getEffectiveToughness()).isEqualTo(2);
        assertThat(millstone.getCard().hasType(CardType.ARTIFACT)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new Toymaker());
        harness.addToBattlefield(player1, new Millstone());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent millstone = findPermanent(player1, "Millstone");
        harness.activateAbility(player1, 0, 0, null, millstone.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        millstone = findPermanent(player1, "Millstone");
        assertThat(millstone.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, millstone)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new Toymaker());
        harness.addToBattlefield(player2, new IronMyr());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Iron Myr");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new Toymaker());
        harness.addToBattlefield(player1, new Millstone());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent millstone = findPermanent(player1, "Millstone");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
