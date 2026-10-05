package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MidvastProtector.class, GrizzlyBears.class})
class MidvastProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants the targeted creature you control protection from the chosen color")
    void etbGrantsProtectionFromChosenColor() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, 0, bearsId);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gqs.findPermanentById(gd, bearsId);
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("The ETB fizzles when its target leaves the battlefield before resolution")
    void etbFizzlesWhenTargetGone() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, 0, bearsId);
        harness.passBothPriorities(); // resolve the creature spell, ETB trigger goes on the stack

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(bearsId));

        harness.passBothPriorities(); // ETB fizzles, so no color is chosen

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionClearedAtEndOfTurn() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gqs.findPermanentById(gd, bearsId);
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("The ETB cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("The entering Protector can target itself and choose any color")
    void canTargetItselfWithAnyColor(CardColor color) {
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID protectorId = harness.getPermanentId(player1, "Midvast Protector");
        harness.handlePermanentChosen(player1, protectorId);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, color.name());

        assertThat(gqs.findPermanentById(gd, protectorId).getProtectionFromColorsUntilEndOfTurn())
                .containsExactly(color);
    }

    @Test
    @DisplayName("The trigger resolves after the Protector leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MidvastProtector());
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> !permanent.getId().equals(target.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("A target that changes to the opponent's control becomes illegal")
    void targetChangingControllerMakesTriggerFizzle() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MidvastProtector());
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARTIFACT", "COLORLESS"})
    @DisplayName("Artifacts and colorless cannot be chosen instead of a color")
    void rejectsNonColorProtectionChoices(String invalidChoice) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MidvastProtector());
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, invalidChoice))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(target.getProtectionFromCardTypes()).isEmpty();
        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isFalse();

        harness.handleListChoice(player1, "GREEN");
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.GREEN);
    }
}

