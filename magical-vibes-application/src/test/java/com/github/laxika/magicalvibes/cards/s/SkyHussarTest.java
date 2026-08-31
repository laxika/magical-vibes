package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SkyHussar.class, CloudSprite.class, Forest.class, GrizzlyBears.class, SuntailHawk.class})
class SkyHussarTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, untaps all creatures its controller controls")
    void entersAndUntapsControlledCreaturesOnly() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new CloudSprite());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new CloudSprite());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        SkyHussar skyHussar = new SkyHussar();
        harness.setHand(player1, List.of(skyHussar));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(skyHussar);
        assertThat(whiteCreature.isTapped()).isTrue();
        assertThat(blueCreature.isTapped()).isTrue();
        assertThat(greenCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(skyHussar, drawnCard);
    }

    @Test
    @DisplayName("Forecast cannot be activated more than once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new CloudSprite());
        harness.setHand(player1, List.of(new SkyHussar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast requires its controller's upkeep and two eligible creatures")
    void forecastChecksTimingAndTapCost() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkyHussar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");

        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
    }
}
