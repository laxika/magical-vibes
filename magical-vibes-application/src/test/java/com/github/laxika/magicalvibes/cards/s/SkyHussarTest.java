package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GnatAlleyCreeper;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
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

@CardUsed({SkyHussar.class, MistralCharger.class, SilkwingScout.class, GnatAlleyCreeper.class})
class SkyHussarTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, untaps all creatures its controller controls")
    void entersAndUntapsControlledCreaturesOnly() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new SilkwingScout());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GnatAlleyCreeper());
        whiteCreature.tap();
        blueCreature.tap();
        opponentCreature.tap();

        harness.setHand(player1, List.of(new SkyHussar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(whiteCreature.isTapped()).isFalse();
        assertThat(blueCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forecast taps two controlled white or blue creatures and draws a card")
    void forecastTapsEligibleCreaturesAndDraws() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new SilkwingScout());
        Permanent ineligibleCreature = harness.addToBattlefieldAndReturn(player1, new GnatAlleyCreeper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MistralCharger());
        SkyHussar skyHussar = new SkyHussar();
        harness.setHand(player1, List.of(skyHussar));
        SkyHussar drawnCard = new SkyHussar();
        harness.setLibrary(player1, List.of(drawnCard));
        advanceToUpkeep(player1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(skyHussar);
        assertThat(whiteCreature.isTapped()).isTrue();
        assertThat(blueCreature.isTapped()).isTrue();
        assertThat(ineligibleCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(skyHussar, drawnCard);
    }

    @Test
    @DisplayName("Forecast cannot use a tapped eligible creature to pay its cost")
    void forecastRequiresTwoUntappedEligibleCreatures() {
        advanceToUpkeep(player1);
        Permanent tappedWhiteCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        Permanent untappedWhiteCreature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        tappedWhiteCreature.tap();
        harness.setHand(player1, List.of(new SkyHussar()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");

        assertThat(tappedWhiteCreature.isTapped()).isTrue();
        assertThat(untappedWhiteCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Forecast cannot be activated more than once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        harness.addToBattlefield(player1, new MistralCharger());
        harness.addToBattlefield(player1, new SilkwingScout());
        harness.setHand(player1, List.of(new SkyHussar()));
        advanceToUpkeep(player1);

        harness.activateHandAbility(player1, 0, null);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast requires its controller's upkeep and two eligible creatures")
    void forecastChecksTimingAndTapCost() {
        harness.addToBattlefield(player1, new MistralCharger());
        harness.addToBattlefield(player1, new GnatAlleyCreeper());
        harness.setHand(player1, List.of(new SkyHussar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");

        advanceToUpkeep(player1);
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
    }
}
