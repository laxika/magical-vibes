package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({AvenWarcraft.class, KrosanVerge.class, SuntailHawk.class})
class AvenWarcraftTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures +0/+2 without threshold")
    void boostsOwnCreaturesWithoutThreshold() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        castAvenWarcraft();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("At threshold, also grants your creatures protection from a chosen color")
    void grantsChosenColorProtectionAtThreshold() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(7));
        castAvenWarcraft();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownLand, CardColor.RED)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.hasProtectionFrom(gd, opposingCreature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Threshold is checked when the instant resolves")
    void thresholdIsCheckedAtResolution() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(6));
        prepareAvenWarcraft();

        harness.castInstant(player1, 0);
        harness.setGraveyard(player1, fillerGraveyard(7));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Threshold counts only the caster's graveyard")
    void thresholdCountsOnlyCastersGraveyard() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(6));
        harness.setGraveyard(player2, fillerGraveyard(7));
        castAvenWarcraft();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("The boost and protection expire at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(7));
        castAvenWarcraft();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLACK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLACK)).isFalse();
    }

    private void castAvenWarcraft() {
        prepareAvenWarcraft();
        harness.castAndResolveInstant(player1, 0);
    }

    private void prepareAvenWarcraft() {
        harness.setHand(player1, List.of(new AvenWarcraft()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> fillerGraveyard(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> (Card) new SuntailHawk())
                .toList();
    }
}
