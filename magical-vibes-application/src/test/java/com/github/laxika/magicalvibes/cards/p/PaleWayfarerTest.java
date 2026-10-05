package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaleWayfarer.class, SafeholdElite.class, Forest.class})
class PaleWayfarerTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{W}{W}, {Q}: target creature gains protection from the chosen color and the source untaps")
    void grantsProtectionAndUntaps() {
        Permanent wayfarer = addTapped(player1, new PaleWayfarer());
        Permanent target = addCreatureReady(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 4);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null).isTrue();
        harness.handleListChoice(player1, "RED");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        // Paying {Q} untapped the source.
        assertThat(wayfarer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("When targeting an opponent's creature, that creature's controller chooses the color")
    void targetControllerChoosesColor() {
        addTapped(player1, new PaleWayfarer());
        Permanent opponentCreature = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 4);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, opponentCreature.getId());
        harness.passBothPriorities();

        // The choice belongs to the target's controller (player2), not the ability's controller.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null).isTrue();
        harness.handleListChoice(player2, "GREEN");

        assertThat(opponentCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new PaleWayfarer());
        Permanent target = addCreatureReady(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 4);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addTapped(player1, new PaleWayfarer());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 4);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionClearedAtEndOfTurn() {
        addTapped(player1, new PaleWayfarer());
        Permanent target = addCreatureReady(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 4);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Untap cost is paid immediately and Pale Wayfarer can target itself")
    void canTargetItselfAndUntapsBeforeResolution() {
        Permanent wayfarer = addTapped(player1, new PaleWayfarer());
        harness.addMana(player1, ManaColor.WHITE, 4);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, wayfarer.getId());

        assertThat(wayfarer.isTapped()).isFalse();
        assertThat(wayfarer.getProtectionFromColorsUntilEndOfTurn()).isEmpty();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(wayfarer.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the untap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player1, new PaleWayfarer());
        wayfarer.setSummoningSick(true);
        wayfarer.tap();
        Permanent target = addCreatureReady(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 4);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(wayfarer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires two white mana even with enough total mana")
    void cannotActivateWithoutEnoughWhiteMana() {
        Permanent wayfarer = addTapped(player1, new PaleWayfarer());
        Permanent target = addCreatureReady(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(wayfarer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
