package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HornedTroll;
import com.github.laxika.magicalvibes.cards.l.Lunge;
import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoryCircle.class, WildJhovall.class, HornedTroll.class, Lunge.class})
class StoryCircleTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Story Circle puts it on the stack as enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Story Circle");
    }

    // ===== Resolving triggers color choice =====

    @Test
    @DisplayName("Resolving Story Circle enters battlefield and awaits color choice")
    void resolvingTriggersColorChoice() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Story Circle");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color sets chosenColor on the permanent")
    void choosingColorSetsOnPermanent() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent perm = findPermanent(player1, "Story Circle");
        assertThat(perm.getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Color choice clears awaiting state")
    void colorChoiceClearsAwaitingState() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Color choice is logged")
    void colorChoiceIsLogged() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses black") && log.contains("Story Circle"));
    }

    // ===== Ability activation =====

    @Test
    @DisplayName("Activating ability with {W} puts prevention on stack")
    void activatingAbilityPutsOnStack() {
        addReadyStoryCircle(player1, CardColor.RED);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Story Circle");
    }

    @Test
    @DisplayName("Resolving ability prompts to choose a source of the chosen color")
    void resolvingAbilityPromptsForChosenSource() {
        addReadyStoryCircle(player1, CardColor.RED);
        Permanent redSource = addCreatureReady(player2, new WildJhovall());
        Permanent greenSource = addCreatureReady(player2, new HornedTroll());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(redSource.getId()).doesNotContain(greenSource.getId());
    }

    @Test
    @DisplayName("Allows choosing a matching red spell on the stack as the source")
    void allowsChoosingMatchingSpellOnStack() {
        addReadyStoryCircle(player1, CardColor.RED);
        Permanent target = addCreatureReady(player1, new HornedTroll());
        Lunge lunge = new Lunge();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(lunge));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, List.of(target.getId(), player1.getId()));
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(lunge.getId());
    }

    // ===== Damage prevention in combat =====

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source")
    void preventsCombatDamageFromChosenSource() {
        addReadyStoryCircle(player2, CardColor.RED);
        Permanent attacker = addCreatureReady(player1, new WildJhovall());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, attacker.getId());
        attacker.setAttacking(true);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a different source is not prevented")
    void doesNotPreventDamageFromUnchosenSource() {
        addReadyStoryCircle(player2, CardColor.RED);
        Permanent chosenSource = addCreatureReady(player1, new WildJhovall());
        Permanent attacker = addCreatureReady(player1, new HornedTroll());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosenSource.getId());
        attacker.setAttacking(true);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player2.getId())
                        && shield.sourceId().equals(chosenSource.getId()));
    }

    // ===== Multiple activations =====

    @Test
    @DisplayName("Multiple activations prevent multiple damage instances")
    void multipleActivationsPreventMultipleInstances() {
        addReadyStoryCircle(player2, CardColor.RED);
        Permanent attacker1 = addCreatureReady(player1, new WildJhovall());
        Permanent attacker2 = addCreatureReady(player1, new WildJhovall());
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, attacker1.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, attacker2.getId());

        attacker1.setAttacking(true);
        attacker2.setAttacking(true);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    // ===== Prevention resets at end of turn =====

    @Test
    @DisplayName("Source-choice prevention resets at end of turn")
    void preventionResetsAtEndOfTurn() {
        addReadyStoryCircle(player1, CardColor.RED);
        Permanent redSource = addCreatureReady(player2, new WildJhovall());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redSource.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        // Advance to end step and pass
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    // ===== Source leaves the battlefield =====

    @Test
    @DisplayName("Source-choice shield remains after Story Circle leaves the battlefield")
    void sourceChoiceRemainsAfterSourceDestroyed() {
        Permanent storyCircle = addReadyStoryCircle(player1, CardColor.RED);
        Permanent redSource = addCreatureReady(player2, new WildJhovall());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redSource.getId());

        gd.playerBattlefields.get(player1.getId()).remove(storyCircle);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player1.getId())
                        && shield.sourceId().equals(redSource.getId()));
    }

    // ===== Helpers =====

    private Permanent addReadyStoryCircle(Player player, CardColor chosenColor) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new StoryCircle());
        perm.setChosenColor(chosenColor);
        return perm;
    }
}

