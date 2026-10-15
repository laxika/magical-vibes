package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BlightSickle;
import com.github.laxika.magicalvibes.cards.i.InescapableBrute;
import com.github.laxika.magicalvibes.cards.t.TurnToMist;
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

@CardUsed({ViridescentWisps.class, InescapableBrute.class, BlightSickle.class, TurnToMist.class})
class ViridescentWispsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving makes target creature green until end of turn")
    void resolvingMakesTargetGreen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new ViridescentWisps()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Resolving gives target creature +1/+0 until end of turn")
    void resolvingBoostsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new ViridescentWisps()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new ViridescentWisps()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new InescapableBrute());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new BlightSickle());
        harness.setHand(player1, List.of(new ViridescentWisps()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
    @Test
    @DisplayName("Color replacement and power boost wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new ViridescentWisps()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("An own creature can be targeted and the caster draws the top card")
    void resolvesOnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new InescapableBrute());
        BlightSickle drawnCard = new BlightSickle();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new ViridescentWisps()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("No card is drawn when the only target leaves before resolution")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        BlightSickle undrawnCard = new BlightSickle();
        harness.setLibrary(player1, List.of(undrawnCard));
        harness.setHand(player1, List.of(new ViridescentWisps()));
        harness.setHand(player2, List.of(new TurnToMist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
        harness.assertInGraveyard(player1, "Viridescent Wisps");
    }
}
