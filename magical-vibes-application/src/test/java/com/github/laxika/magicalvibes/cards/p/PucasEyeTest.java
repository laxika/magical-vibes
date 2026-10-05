package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PucasEye.class, PrismwakeMerrow.class})
class PucasEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering draws a card, then choosing a color makes Puca's Eye that color")
    void entersDrawsThenBecomesChosenColor() {
        harness.setHand(player1, List.of(new PucasEye()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBeforeCast = gd.playerHands.get(player1.getId()).size();
        int deckBeforeCast = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCast);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeCast - 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        Permanent eye = findPermanent(player1, "Puca's Eye");
        assertThat(eye.getChosenColor()).isEqualTo(CardColor.GREEN);
        assertThat(gqs.getEffectiveColors(gd, eye)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("The draw ability works when permanents provide all five colors")
    void drawAbilityRequiresAndUsesFiveColors() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new PucasEye());
        eye.setChosenColor(CardColor.GREEN);
        addColoredPermanent(player1, CardColor.WHITE);
        addColoredPermanent(player1, CardColor.BLUE);
        addColoredPermanent(player1, CardColor.BLACK);
        addColoredPermanent(player1, CardColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(eye.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The draw ability cannot be activated without all five colors")
    void drawAbilityRequiresAllFiveColors() {
        harness.addToBattlefieldAndReturn(player1, new PucasEye());
        addColoredPermanent(player1, CardColor.WHITE);
        addColoredPermanent(player1, CardColor.BLUE);
        addColoredPermanent(player1, CardColor.BLACK);
        addColoredPermanent(player1, CardColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("five colors");
    }

    private void addColoredPermanent(Player player, CardColor color) {
        harness.addToBattlefieldAndReturn(player, new PucasEye()).setChosenColor(color);
    }

    @Test
    @DisplayName("Choosing the entry color completes the ability without triggering it again")
    void choosingColorDoesNotRetriggerEntryAbility() {
        harness.setHand(player1, List.of(new PucasEye()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Colors controlled only by the opponent do not allow activation")
    void opponentColorsDoNotCount() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new PucasEye());
        eye.setChosenColor(CardColor.GREEN);
        for (CardColor color : List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED)) {
            addColoredPermanent(player2, color);
        }
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("five colors");
        assertThat(eye.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Losing a color after activation does not prevent the draw")
    void activationRestrictionIsNotCheckedAtResolution() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new PucasEye());
        eye.setChosenColor(CardColor.GREEN);
        for (CardColor color : List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED)) {
            addColoredPermanent(player1, color);
        }
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("One permanent with all five colors satisfies the activation restriction")
    void oneFiveColorPermanentAllowsActivation() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new PucasEye());
        harness.setHand(player1, List.of(new PrismwakeMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, eye.getId());
        resolveAllTriggers();
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness.handleListChoice(player1, color);
        }
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(eye.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The entry trigger's color change overrides a color change resolved in response")
    void entryColorChangeUsesResolutionTimestamp() {
        harness.setHand(player1, List.of(new PucasEye()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent eye = findPermanent(player1, "Puca's Eye");

        harness.setHand(player1, List.of(new PrismwakeMerrow()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.ensurePriority(player1);
        harness.castCreature(player1, 0, 0, eye.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");
        assertThat(gqs.getEffectiveColors(gd, eye)).containsExactly(CardColor.RED);

        resolveAllTriggers();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.getEffectiveColors(gd, eye)).containsExactly(CardColor.GREEN);
    }
}
