package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EmberShot;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenWarcraft.class, EmberShot.class, KrosanVerge.class, SuntailHawk.class})
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
        harness.castFromHand(player1, new AvenWarcraft(), "{2}{W}");
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
        harness.castFromHand(player1, new AvenWarcraft(), "{2}{W}");
        harness.passBothPriorities();
    }

    private List<Card> fillerGraveyard(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> (Card) new SuntailHawk())
                .toList();
    }

    @Test
    @DisplayName("Does not grant the threshold protection with only six cards in your graveyard")
    void doesNotGrantProtectionBelowThreshold() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(6));
        harness.setGraveyard(player2, fillerGraveyard(7));

        castAvenWarcraft();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Chosen protection prevents a spell of that color from targeting your creatures")
    void chosenProtectionPreventsChosenColorSpellFromTargeting() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(7));
        castAvenWarcraft();
        harness.handleListChoice(player1, CardColor.RED.name());

        harness.setHand(player2, List.of(new EmberShot()));
        harness.addMana(player2, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("The boost and protection wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(7));
        castAvenWarcraft();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Losing threshold before resolution removes only the protection clause")
    void losingThresholdBeforeResolutionStillBoostsCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(7));
        harness.castFromHand(player1, new AvenWarcraft(), "{2}{W}");
        harness.setGraveyard(player1, fillerGraveyard(6));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering before resolution receive both effects")
    void creaturesEnteringBeforeResolutionReceiveBothEffects() {
        harness.setGraveyard(player1, fillerGraveyard(7));
        harness.castFromHand(player1, new AvenWarcraft(), "{2}{W}");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither effect")
    void creaturesEnteringAfterResolutionReceiveNeitherEffect() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(7));
        castAvenWarcraft();
        harness.handleListChoice(player1, "RED");
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, original, CardColor.RED)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(1);
        assertThat(gqs.hasProtectionFrom(gd, newcomer, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection persists if threshold is lost after resolution")
    void protectionPersistsAfterLosingThreshold() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, fillerGraveyard(7));
        castAvenWarcraft();
        harness.handleListChoice(player1, "BLUE");
        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("At threshold a color is chosen even when you control no creatures")
    void choosesColorWithoutCreatures() {
        harness.setGraveyard(player1, fillerGraveyard(7));
        castAvenWarcraft();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Aven Warcraft");
    }
}
