package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhisperBloodLiturgist.class, AngelOfMercy.class, GrizzlyBears.class,
        HolyDay.class, LlanowarElves.class})
class WhisperBloodLiturgistTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Whisper puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new WhisperBloodLiturgist()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Whisper, Blood Liturgist");
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
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
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

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));

        // First choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears);

        // Second choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, elves);

        // Ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        // Sacrificed creatures should be gone, Angel of Mercy remains
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Angel of Mercy");
    }

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

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertOnBattlefield(player1, "Angel of Mercy");
        harness.assertNotInGraveyard(player1, "Angel of Mercy");
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

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angel of Mercy");
    }

    @Test
    @DisplayName("Cannot activate with an empty graveyard even when two creatures can be sacrificed")
    void cannotActivateWithEmptyGraveyard() {
        Permanent whisper = addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(whisper.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

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

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();

        // Angel of Mercy's ETB (gain 3 life) should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel of Mercy");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Cannot activate with only Whisper on battlefield")
    void cannotActivateWithOnlyWhisper() {
        addReadyWhisper(player1);
        harness.setGraveyard(player1, List.of(new AngelOfMercy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId())))
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

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId())))
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

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotChooseNonCreatureFromGraveyard() {
        Permanent whisper = addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        HolyDay holyDay = new HolyDay();
        harness.setGraveyard(player1, List.of(holyDay, new AngelOfMercy()));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(holyDay.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(whisper.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

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

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();

        // Angel of Mercy ETB is on the stack — resolve it
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

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
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.handlePermanentChosen(player1, bears);
        harness.handlePermanentChosen(player1, elves);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angel of Mercy");
    }

    @Test
    @DisplayName("Whisper can sacrifice itself and another creature to return its announced target")
    void canSacrificeWhisperItself() {
        Permanent whisper = addReadyWhisper(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        AngelOfMercy target = new AngelOfMercy();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.handlePermanentChosen(player1, whisper.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Whisper, Blood Liturgist");
        harness.assertInGraveyard(player1, "Whisper, Blood Liturgist");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Angel of Mercy");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target a creature in the opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Permanent whisper = addReadyWhisper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        AngelOfMercy opponentCard = new AngelOfMercy();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(opponentCard));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(whisper.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An ability with a removed target cannot return a different creature")
    void removedTargetDoesNotAllowChoosingAnotherCreature() {
        addReadyWhisper(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        AngelOfMercy target = new AngelOfMercy();
        harness.setGraveyard(player1, List.of(target, new GrizzlyBears()));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, elves.getId());
        // Model the announced target being exiled in response.
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel of Mercy");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Tapped creatures can be sacrificed and the ability taps Whisper")
    void canSacrificeTappedCreatures() {
        Permanent whisper = addReadyWhisper(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        bears.tap();
        elves.tap();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, elves.getId());

        assertThat(whisper.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyWhisper(Player player) {
        Permanent whisper = harness.addToBattlefieldAndReturn(player, new WhisperBloodLiturgist());
        whisper.setSummoningSick(false);
        return whisper;
    }
}
