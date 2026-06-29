package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuriokSurvivorsTest extends BaseCardTest {

    /**
     * Casts Auriok Survivors and resolves it onto the battlefield, then accepts the first
     * may ability (return Equipment) so the ETB triggered ability is placed on the stack.
     */
    private void castAndAcceptMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AuriokSurvivors()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Auriok Survivors has ETB MayEffect returning Equipment from graveyard")
    void hasCorrectProperties() {
        AuriokSurvivors card = new AuriokSurvivors();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect returnEffect = (ReturnCardFromGraveyardEffect) mayEffect.wrapped();
        assertThat(returnEffect.attachToSource()).isTrue();
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving Auriok Survivors triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.setHand(player1, List.of(new AuriokSurvivors()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setGraveyard(player1, List.of(new DarksteelAxe()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may ability resolves inner effect inline — graveyard choice prompt appears")
    void acceptingMayResolvesInline() {
        harness.setGraveyard(player1, List.of(new DarksteelAxe()));
        castAndAcceptMay();

        // Inner effect resolved inline — graveyard choice prompt appears immediately
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Declining may ability does not return Equipment")
    void decliningMaySkipsAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AuriokSurvivors()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setGraveyard(player1, List.of(new DarksteelAxe()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Auriok Survivors");
        harness.assertInGraveyard(player1, "Darksteel Axe");
    }

    // ===== Returns Equipment from graveyard to battlefield and attaches =====

    @Test
    @DisplayName("Returns Equipment from graveyard to battlefield and attaches to Auriok Survivors")
    void returnsEquipmentAndAttaches() {
        harness.setGraveyard(player1, List.of(new DarksteelAxe()));
        castAndAcceptMay();

        // Inner effect resolved inline → graveyard choice prompt
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Choose the Equipment (index 0) → triggers second may prompt for attachment
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Accept attachment
        harness.handleMayAbilityChosen(player1, true);

        // Equipment on battlefield and attached to Auriok Survivors
        harness.assertOnBattlefield(player1, "Darksteel Axe");
        harness.assertNotInGraveyard(player1, "Darksteel Axe");

        Permanent axePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Darksteel Axe"))
                .findFirst().orElse(null);
        Permanent survivorsPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Auriok Survivors"))
                .findFirst().orElse(null);
        assertThat(axePerm).isNotNull();
        assertThat(survivorsPerm).isNotNull();
        assertThat(axePerm.getAttachedTo()).isEqualTo(survivorsPerm.getId());
    }

    @Test
    @DisplayName("Player can decline attachment — Equipment stays on battlefield unattached")
    void decliningAttachmentLeavesEquipmentUnattached() {
        harness.setGraveyard(player1, List.of(new DarksteelAxe()));
        castAndAcceptMay();

        harness.handleGraveyardCardChosen(player1, 0);

        // Decline attachment
        harness.handleMayAbilityChosen(player1, false);

        // Equipment on battlefield but NOT attached
        harness.assertOnBattlefield(player1, "Darksteel Axe");
        Permanent axePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Darksteel Axe"))
                .findFirst().orElse(null);
        assertThat(axePerm).isNotNull();
        assertThat(axePerm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Choosing specific Equipment when multiple are in graveyard")
    void choosesSpecificEquipmentFromGraveyard() {
        harness.setGraveyard(player1, List.of(new DarksteelAxe(), new LeoninScimitar()));
        castAndAcceptMay();

        // Choose Leonin Scimitar (index 1)
        harness.handleGraveyardCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, true); // accept attachment

        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Darksteel Axe");

        Permanent scimitarPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leonin Scimitar"))
                .findFirst().orElse(null);
        Permanent survivorsPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Auriok Survivors"))
                .findFirst().orElse(null);
        assertThat(scimitarPerm).isNotNull();
        assertThat(scimitarPerm.getAttachedTo()).isEqualTo(survivorsPerm.getId());
    }

    // ===== Non-Equipment cards filtered out =====

    @Test
    @DisplayName("ETB resolves with no effect if graveyard has no Equipment")
    void noEffectWithNoEquipmentInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no Equipment"));
    }

    @Test
    @DisplayName("ETB resolves with no effect if graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        castAndAcceptMay();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Only Equipment cards are valid choices")
    void onlyEquipmentCardsAreValid() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DarksteelAxe()));
        castAndAcceptMay();

        // Index 0 is Grizzly Bears (creature, not equipment) — not a valid choice
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    // ===== Equipment returns even if Auriok Survivors leaves =====

    @Test
    @DisplayName("Equipment returns to battlefield without attachment prompt if Auriok Survivors leaves")
    void equipmentReturnedWithoutAttachIfSourceGone() {
        harness.setGraveyard(player1, List.of(new DarksteelAxe()));
        castAndAcceptMay();

        // Remove Auriok Survivors from battlefield — inner effect already resolved inline
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Auriok Survivors"));

        harness.handleGraveyardCardChosen(player1, 0);

        // Equipment is on battlefield but NOT attached — no second may prompt since source is gone
        harness.assertOnBattlefield(player1, "Darksteel Axe");
        Permanent axePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Darksteel Axe"))
                .findFirst().orElse(null);
        assertThat(axePerm).isNotNull();
        assertThat(axePerm.getAttachedTo()).isNull();
    }

    // ===== Stack cleanup =====

    @Test
    @DisplayName("Stack is empty after full resolution with attachment")
    void stackIsEmptyAfterFullResolution() {
        harness.setGraveyard(player1, List.of(new DarksteelAxe()));
        castAndAcceptMay();

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true); // accept attachment

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Auriok Survivors remains on battlefield after returning Equipment")
    void survivorsRemainsOnBattlefield() {
        harness.setGraveyard(player1, List.of(new DarksteelAxe()));
        castAndAcceptMay();

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Auriok Survivors");
    }
}
