package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BlightSickle;
import com.github.laxika.magicalvibes.cards.i.InescapableBrute;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({NiveousWisps.class, InescapableBrute.class, BlightSickle.class})
class NiveousWispsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving makes target creature white until end of turn")
    void resolvingMakesTargetWhite() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new NiveousWisps()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        // "Becomes white" replaces the colors (CR 105.3), applied by the CR 613 layer engine.
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Resolving taps the target creature")
    void resolvingTapsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new NiveousWisps()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new NiveousWisps()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new NiveousWisps()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        // A legal creature target exists, so the spell is playable; only the noncreature is rejected.
        harness.addToBattlefield(player2, new InescapableBrute());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new BlightSickle());
        harness.setHand(player1, List.of(new NiveousWisps()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An already-tapped creature you control still becomes white and allows the draw")
    void resolvesOnAlreadyTappedOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new InescapableBrute());
        target.setTapped(true);
        BlightSickle drawnCard = new BlightSickle();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new NiveousWisps()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("No card is drawn when the sole target leaves the battlefield before resolution")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        BlightSickle drawnCard = new BlightSickle();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new NiveousWisps()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Niveous Wisps");
    }
}
