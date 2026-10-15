package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PheresBandCentaurs;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GodsWilling.class, PheresBandCentaurs.class})
class GodsWillingTest extends BaseCardTest {

    @Test
    @DisplayName("Protects the targeted creature and scries 1")
    void protectsTargetAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PheresBandCentaurs());
        harness.setHand(player1, List.of(new GodsWilling()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        Card originalTop = gd.playerDecks.get(player1.getId()).getFirst();

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(originalTop);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new PheresBandCentaurs());
        harness.setHand(player1, List.of(new GodsWilling()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Pheres-Band Centaurs")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PheresBandCentaurs());
        harness.setHand(player1, List.of(new GodsWilling()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "BLUE");
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Choosing white still allows scrying and putting the card on the bottom")
    void choosingWhiteStillScriesToBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PheresBandCentaurs());
        harness.setHand(player1, List.of(new GodsWilling()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        Card top = new GodsWilling();
        Card next = new PheresBandCentaurs();
        harness.setLibrary(player1, List.of(top, next));

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "WHITE");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Gods Willing");
    }

    @Test
    @DisplayName("An empty library does not prevent granting protection or finishing resolution")
    void emptyLibraryStillGrantsProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PheresBandCentaurs());
        harness.setHand(player1, List.of(new GodsWilling()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "BLACK");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Gods Willing");
    }

    @Test
    @DisplayName("Does not scry when its only target leaves before resolution")
    void missingTargetPreventsScry() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PheresBandCentaurs());
        harness.setHand(player1, List.of(new GodsWilling()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        Card top = new GodsWilling();
        harness.setLibrary(player1, List.of(top));

        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Gods Willing");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARTIFACT", "COLORLESS"})
    @DisplayName("Rejects protection choices that are not colors")
    void rejectsNonColorProtectionChoices(String choice) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PheresBandCentaurs());
        harness.setHand(player1, List.of(new GodsWilling()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        PendingInteraction.ColorChoice prompt =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(prompt.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        assertThatThrownBy(() -> harness.handleListChoice(player1, choice))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isSameAs(prompt);
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(target.getProtectionFromCardTypes()).isEmpty();
        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isFalse();

        harness.handleListChoice(player1, "GREEN");
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.GREEN);
        assertThat(gd.stack).isEmpty();
    }
}
