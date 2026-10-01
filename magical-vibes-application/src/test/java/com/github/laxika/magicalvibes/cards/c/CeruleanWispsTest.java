package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CeruleanWisps.class, InescapableBrute.class, BlightSickle.class})
class CeruleanWispsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving makes target creature blue until end of turn")
    void resolvingMakesTargetBlue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        // "Becomes blue" replaces the colors (CR 105.3), applied by the CR 613 layer engine.
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Resolving untaps the target creature")
    void resolvingUntapsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        target.tap();

        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        // A legal creature target exists, so the spell is playable; only the noncreature is rejected.
        harness.addToBattlefield(player2, new InescapableBrute());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new BlightSickle());
        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
