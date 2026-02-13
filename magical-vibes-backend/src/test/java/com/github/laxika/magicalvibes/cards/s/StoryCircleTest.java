package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StoryCircleTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card(name, CardType.CREATURE, "{1}", color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Story Circle has correct card properties")
    void hasCorrectProperties() {
        StoryCircle card = new StoryCircle();

        assertThat(card.getName()).isEqualTo("Story Circle");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseColorOnEnterEffect.class);
        assertThat(card.getEffects(EffectSlot.MANA_ACTIVATED_ABILITY)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.MANA_ACTIVATED_ABILITY).getFirst())
                .isInstanceOf(PreventNextColorDamageToControllerEffect.class);
        assertThat(card.getManaActivatedAbilityCost()).isEqualTo("{W}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Story Circle puts it on the stack as enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, "W", 3);

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
        harness.addMana(player1, "W", 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Story Circle"));
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.awaitingColorChoicePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color sets chosenColor on the permanent")
    void choosingColorSetsOnPermanent() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, "W", 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "RED");

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Story Circle"))
                .findFirst().orElseThrow();
        assertThat(perm.getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Color choice clears awaiting state")
    void colorChoiceClearsAwaitingState() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, "W", 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "BLUE");

        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.awaitingColorChoicePlayerId).isNull();
        assertThat(gd.awaitingColorChoicePermanentId).isNull();
    }

    @Test
    @DisplayName("Color choice is logged")
    void colorChoiceIsLogged() {
        harness.setHand(player1, List.of(new StoryCircle()));
        harness.addMana(player1, "W", 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "BLACK");

        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses black") && log.contains("Story Circle"));
    }

    // ===== Ability activation =====

    @Test
    @DisplayName("Activating ability with {W} puts prevention on stack")
    void activatingAbilityPutsOnStack() {
        Permanent storyCircle = addReadyStoryCircle(player1, CardColor.RED);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Story Circle");
    }

    @Test
    @DisplayName("Resolving ability adds prevention count for chosen color")
    void resolvingAbilityAddsPreventionCount() {
        Permanent storyCircle = addReadyStoryCircle(player1, CardColor.RED);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerColorDamagePreventionCount.get(player1.getId()))
                .containsEntry(CardColor.RED, 1);
    }

    // ===== Damage prevention in combat =====

    @Test
    @DisplayName("Prevents combat damage from creature of chosen color")
    void preventsCombatDamageFromChosenColor() {
        addReadyStoryCircle(player2, CardColor.RED);
        harness.addMana(player2, "W", 1);

        // Activate Story Circle
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        // Set up combat: red creature attacks player2
        Permanent attacker = new Permanent(createCreature("Fire Elemental", 5, 4, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        gs.passPriority(gd, player1);
        gs.passPriority(gd, player2);

        // Player2 takes no damage — red damage prevented
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does NOT prevent combat damage from non-chosen color")
    void doesNotPreventDamageFromNonChosenColor() {
        addReadyStoryCircle(player2, CardColor.RED);
        harness.addMana(player2, "W", 1);

        // Activate Story Circle (prevents red)
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        // Set up combat: GREEN creature attacks player2
        Permanent attacker = new Permanent(createCreature("Big Green", 3, 3, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        gs.passPriority(gd, player1);
        gs.passPriority(gd, player2);

        // Player2 takes 3 damage — green is not the chosen color
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    // ===== Multiple activations =====

    @Test
    @DisplayName("Multiple activations prevent multiple damage instances")
    void multipleActivationsPreventMultipleInstances() {
        addReadyStoryCircle(player2, CardColor.RED);
        harness.addMana(player2, "W", 2);

        // Activate twice
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerColorDamagePreventionCount.get(player2.getId()))
                .containsEntry(CardColor.RED, 2);

        // Two red creatures attack
        Permanent attacker1 = new Permanent(createCreature("Red One", 2, 2, CardColor.RED));
        attacker1.setSummoningSick(false);
        attacker1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker1);

        Permanent attacker2 = new Permanent(createCreature("Red Two", 3, 3, CardColor.RED));
        attacker2.setSummoningSick(false);
        attacker2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        gs.passPriority(gd, player1);
        gs.passPriority(gd, player2);

        // Both prevented — life unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    // ===== Prevention resets at end of turn =====

    @Test
    @DisplayName("Prevention count resets at end of turn")
    void preventionResetsAtEndOfTurn() {
        addReadyStoryCircle(player1, CardColor.RED);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerColorDamagePreventionCount.get(player1.getId()))
                .containsEntry(CardColor.RED, 1);

        // Advance to end step and pass
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerColorDamagePreventionCount).isEmpty();
    }

    // ===== Empty battlefield safety =====

    @Test
    @DisplayName("Ability still resolves after Story Circle is destroyed")
    void abilityResolvesAfterSourceDestroyed() {
        Permanent storyCircle = addReadyStoryCircle(player1, CardColor.RED);
        harness.addMana(player1, "W", 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Story Circle before resolution (e.g. destroyed in response)
        gd.playerBattlefields.get(player1.getId()).remove(storyCircle);

        // Ability resolves independently — prevention should still be added
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerColorDamagePreventionCount.get(player1.getId()))
                .containsEntry(CardColor.RED, 1);
    }

    // ===== Helpers =====

    private Permanent addReadyStoryCircle(Player player, CardColor chosenColor) {
        StoryCircle card = new StoryCircle();
        Permanent perm = new Permanent(card);
        perm.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
