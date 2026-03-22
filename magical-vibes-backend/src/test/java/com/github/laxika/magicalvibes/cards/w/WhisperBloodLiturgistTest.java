package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhisperBloodLiturgistTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Whisper has correct activated ability")
    void hasCorrectAbility() {
        WhisperBloodLiturgist card = new WhisperBloodLiturgist();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeMultiplePermanentsCost.class);
        SacrificeMultiplePermanentsCost sacCost = (SacrificeMultiplePermanentsCost) ability.getEffects().get(0);
        assertThat(sacCost.count()).isEqualTo(2);
        assertThat(ability.getEffects().get(1)).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Whisper puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new WhisperBloodLiturgist()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Whisper, Blood Liturgist");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Whisper, Blood Liturgist"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new WhisperBloodLiturgist()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Activating ability — sacrifice choice flow =====

    @Test
    @DisplayName("Prompts for sacrifice choice when more than 2 creatures available")
    void promptsForChoiceWhenMoreThanTwoCreatures() {
        addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Whisper + GrizzlyBears + LlanowarElves = 3 creatures, needs choice
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Completing two sacrifice choices puts ability on stack")
    void completingTwoSacrificesPutsAbilityOnStack() {
        addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new AngelOfMercy());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bears = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID elves = gd.playerBattlefields.get(player1.getId()).get(2).getId();

        harness.activateAbility(player1, 0, null, null);

        // First choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, bears);

        // Second choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, elves);

        // Ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        // Sacrificed creatures should be gone, Angel of Mercy remains
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel of Mercy"));
    }

    // ===== Resolution — returning creature from graveyard =====

    @Test
    @DisplayName("Returns creature from graveyard to battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bears = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID elves = gd.playerBattlefields.get(player1.getId()).get(2).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel of Mercy"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Angel of Mercy"));
    }

    @Test
    @DisplayName("Choosing specific creature when multiple are in graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new AngelOfMercy(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bears = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID elves = gd.playerBattlefields.get(player1.getId()).get(2).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();

        // Graveyard after sacrifice: [AngelOfMercy, GrizzlyBears, GrizzlyBears, LlanowarElves]
        // Choose Angel of Mercy at index 0
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel of Mercy"));
    }

    @Test
    @DisplayName("Sacrificed creatures are valid graveyard choices at resolution")
    void sacrificedCreaturesAreValidGraveyardChoices() {
        addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        // Empty graveyard — sacrificed creatures will be the only options
        harness.setGraveyard(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bears = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID elves = gd.playerBattlefields.get(player1.getId()).get(2).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();

        // Graveyard should contain the two sacrificed creatures
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Choose one of the sacrificed creatures
        harness.handleGraveyardCardChosen(player1, 0);

        // One of the sacrificed creatures should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears")
                        || p.getCard().getName().equals("Llanowar Elves"))
                .hasSize(1);
    }

    // ===== ETB on returned creature =====

    @Test
    @DisplayName("Returned creature's ETB ability triggers")
    void returnedCreatureTriggersETB() {
        addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bears = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID elves = gd.playerBattlefields.get(player1.getId()).get(2).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        // Angel of Mercy's ETB (gain 3 life) should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel of Mercy");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    // ===== Validation — cannot activate =====

    @Test
    @DisplayName("Cannot activate with only Whisper on battlefield")
    void cannotActivateWithOnlyWhisper() {
        addReadyWhisper(player1);
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent whisper = addReadyWhisper(player1);
        whisper.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        WhisperBloodLiturgist card = new WhisperBloodLiturgist();
        harness.addToBattlefield(player1, card);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Invalid graveyard choice =====

    @Test
    @DisplayName("Cannot choose non-creature card from graveyard")
    void cannotChooseNonCreatureFromGraveyard() {
        addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new HolyDay(), new AngelOfMercy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bears = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID elves = gd.playerBattlefields.get(player1.getId()).get(2).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();

        // HolyDay is at index 0, not a creature
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    // ===== Stack is empty after full resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bears = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID elves = gd.playerBattlefields.get(player1.getId()).get(2).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        // Angel of Mercy ETB is on the stack — resolve it
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== No mana cost for ability =====

    @Test
    @DisplayName("Ability does not require mana")
    void abilityDoesNotRequireMana() {
        addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bears = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID elves = gd.playerBattlefields.get(player1.getId()).get(2).getId();

        // No mana added — should still work
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel of Mercy"));
    }

    // ===== Helpers =====

    private Permanent addReadyWhisper(Player player) {
        WhisperBloodLiturgist card = new WhisperBloodLiturgist();
        Permanent whisper = new Permanent(card);
        whisper.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(whisper);
        return whisper;
    }
}
