package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlightMamba;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardWithKeywordFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorpseCurTest extends BaseCardTest {

    /**
     * Casts Corpse Cur and resolves it onto the battlefield, then accepts the may ability.
     */
    private void castAndAcceptMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CorpseCur()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → ETB on stack
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has ETB may ability to return creature with infect from graveyard")
    void hasCorrectEffect() {
        CorpseCur card = new CorpseCur();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ReturnCardWithKeywordFromGraveyardToHandEffect.class);
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving Corpse Cur triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.setGraveyard(player1, List.of(new BlightMamba()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CorpseCur()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve → may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Declining may ability does not return anything")
    void decliningMaySkipsAbility() {
        harness.setGraveyard(player1, List.of(new BlightMamba()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CorpseCur()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blight Mamba"));
    }

    // ===== Graveyard return =====

    @Test
    @DisplayName("Returns creature with infect from graveyard to hand")
    void returnsInfectCreatureFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new BlightMamba()));
        castAndAcceptMay();

        harness.passBothPriorities(); // resolve ETB → graveyard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blight Mamba"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Blight Mamba"));
    }

    @Test
    @DisplayName("Chooses specific infect creature when multiple are in graveyard")
    void choosesSpecificInfectCreature() {
        harness.setGraveyard(player1, List.of(new BlightMamba(), new ContagiousNim()));
        castAndAcceptMay();

        harness.passBothPriorities();

        // Choose Contagious Nim (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Contagious Nim"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blight Mamba"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Contagious Nim"));
    }

    // ===== Filtering =====

    @Test
    @DisplayName("Cannot return creature without infect")
    void cannotReturnCreatureWithoutInfect() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new BlightMamba()));
        castAndAcceptMay();

        harness.passBothPriorities();

        // Index 0 is Grizzly Bears (no infect) — not a valid choice
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("No effect if graveyard has no creature cards with infect")
    void noEffectWithNoInfectCreaturesInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HolyDay()));
        castAndAcceptMay();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no creature card with infect"));
    }

    @Test
    @DisplayName("No effect if graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        castAndAcceptMay();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no creature card with infect"));
    }

    @Test
    @DisplayName("Player can decline graveyard choice")
    void playerCanDeclineGraveyardChoice() {
        harness.setGraveyard(player1, List.of(new BlightMamba()));
        castAndAcceptMay();

        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blight Mamba"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Blight Mamba"));
    }
}
