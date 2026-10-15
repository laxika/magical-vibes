package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherTradewinds;
import com.github.laxika.magicalvibes.cards.l.LoamLion;
import com.github.laxika.magicalvibes.cards.v.VeteransReflexes;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SejiriSteppe.class, LoamLion.class, AetherTradewinds.class, VeteransReflexes.class})
class SejiriSteppeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gives a target creature you control protection from the chosen color")
    void entersTappedAndGrantsProtection() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        harness.setHand(player1, List.of(new SejiriSteppe()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(findPermanent(player1, "Sejiri Steppe").isTapped()).isTrue();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new LoamLion());
        harness.setHand(player1, List.of(new SejiriSteppe()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownBears.getId());
    }

    @Test
    @DisplayName("Tapping adds one white mana")
    void tappingAddsWhiteMana() {
        Permanent steppe = harness.addToBattlefieldAndReturn(player1, new SejiriSteppe());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(steppe.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        harness.setHand(player1, List.of(new SejiriSteppe()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Enters tapped without prompting when no creature can be targeted")
    void entersWithoutLegalTarget() {
        harness.addToBattlefield(player2, new LoamLion());
        harness.setHand(player1, List.of(new SejiriSteppe()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Sejiri Steppe").isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land itself is not a legal creature target")
    void cannotTargetNoncreature() {
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        harness.setHand(player1, List.of(new SejiriSteppe()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                findPermanent(player1, "Sejiri Steppe").getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, lion.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        assertThat(lion.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Does not choose a color when the target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent ownLion = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        Permanent opposingLion = harness.addToBattlefieldAndReturn(player2, new LoamLion());
        harness.setHand(player1, List.of(new SejiriSteppe(), new AetherTradewinds()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, ownLion.getId());

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, List.of(ownLion.getId(), opposingLion.getId()));
        harness.assertInHand(player1, "Loam Lion");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(ownLion.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Granted protection prevents targeting by a spell of the chosen color")
    void protectionPreventsWhiteSpellTargeting() {
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        harness.setHand(player1, List.of(new SejiriSteppe(), new VeteransReflexes()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, lion.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, lion.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARTIFACT", "COLORLESS"})
    @DisplayName("Protection choice rejects artifacts and colorless because neither is a color")
    void rejectsNoncolorProtectionChoice(String choice) {
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new LoamLion());
        harness.setHand(player1, List.of(new SejiriSteppe()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, lion.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, choice))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "GREEN");
        assertThat(lion.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.GREEN);
    }
}
