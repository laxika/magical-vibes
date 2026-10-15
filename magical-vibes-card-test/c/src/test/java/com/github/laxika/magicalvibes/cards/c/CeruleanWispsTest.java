package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CeruleanWisps.class, InescapableBrute.class, BlightSickle.class, TurnToMist.class})
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

        harness.passUntil(player2, TurnStep.UPKEEP);

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

    @Test
    @DisplayName("An already untapped creature is legal and the caster still draws")
    void resolvesOnUntappedOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new InescapableBrute());
        BlightSickle drawnCard = new BlightSickle();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("No card is drawn when the only target leaves before resolution")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InescapableBrute());
        BlightSickle undrawnCard = new BlightSickle();
        harness.setLibrary(player1, List.of(undrawnCard));
        harness.setHand(player1, List.of(new CeruleanWisps()));
        harness.setHand(player2, List.of(new TurnToMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
        harness.assertInGraveyard(player1, "Cerulean Wisps");
    }
}
