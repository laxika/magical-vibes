package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NavigatorsCompassTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Navigator's Compass has ETB gain life and activated ability")
    void hasCorrectProperties() {
        NavigatorsCompass card = new NavigatorsCompass();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(GainLifeEffect.class);
        GainLifeEffect lifeEffect = (GainLifeEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(lifeEffect.amount()).isEqualTo(3);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(GrantBasicLandTypeToTargetEffect.class);
    }

    // ===== ETB life gain =====

    @Test
    @DisplayName("Entering the battlefield triggers gain 3 life")
    void etbGainsLife() {
        castCompass();
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land")
    void activatingAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Navigator's Compass");
        assertThat(entry.getTargetId()).isEqualTo(forestId);
    }

    @Test
    @DisplayName("Resolving ability prompts for basic land type choice")
    void resolvingAbilityPromptsForChoice() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.colorChoiceContext()).isInstanceOf(ChoiceContext.AddBasicLandTypeChoice.class);
    }

    @Test
    @DisplayName("Choosing Plains adds Plains subtype to target Forest")
    void choosingPlainsAddsSubtype() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "PLAINS");

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getTransientSubtypes()).contains(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("Target land gains temporary mana ability for chosen type")
    void targetLandGainsTemporaryManaAbility() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "PLAINS");

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getTemporaryActivatedAbilities()).hasSize(1);
        assertThat(forest.getTemporaryActivatedAbilities().getFirst().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Target land can produce new mana type via temporary ability")
    void targetLandCanProduceNewManaType() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "PLAINS");

        // The forest now has its original tap ability (Green) and a temporary ability (White)
        // Activate the temporary ability (ability index 0 on the Forest = its only activated ability)
        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                gqs.findPermanentById(gd, forestId));
        harness.activateAbility(player1, forestIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing Swamp for a Mountain adds Swamp subtype")
    void choosingSwampForMountain() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Mountain());
        UUID mountainId = harness.getPermanentId(player1, "Mountain");

        harness.activateAbility(player1, 0, null, mountainId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SWAMP");

        Permanent mountain = gqs.findPermanentById(gd, mountainId);
        assertThat(mountain.getTransientSubtypes()).contains(CardSubtype.SWAMP);
    }

    // ===== Until end of turn =====

    @Test
    @DisplayName("Added subtype and temporary ability are cleared at end of turn")
    void effectsClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getTransientSubtypes()).contains(CardSubtype.ISLAND);
        assertThat(forest.getTemporaryActivatedAbilities()).hasSize(1);

        // Simulate end-of-turn cleanup
        forest.resetModifiers();

        assertThat(forest.getTransientSubtypes()).isEmpty();
        assertThat(forest.getTemporaryActivatedAbilities()).isEmpty();
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target a land controlled by the opponent")
    void cannotTargetOpponentLand() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player2, new Forest());
        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentForestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Compass taps when ability is activated")
    void compassTapsOnActivation() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);

        Permanent compass = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Navigator's Compass"))
                .findFirst().orElseThrow();
        assertThat(compass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability when Compass is already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        // First activation
        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "PLAINS");

        // Try to activate again while still tapped
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Game log records the land type change")
    void gameLogRecordsTypeChange() {
        harness.addToBattlefield(player1, new NavigatorsCompass());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Forest") && log.contains("Island") && log.contains("until end of turn"));
    }

    // ===== Helpers =====

    private void castCompass() {
        harness.setHand(player1, List.of(new NavigatorsCompass()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
    }
}
