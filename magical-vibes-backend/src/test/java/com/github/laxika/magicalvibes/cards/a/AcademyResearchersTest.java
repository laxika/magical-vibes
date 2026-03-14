package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AcademyResearchersTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Academy Researchers has correct card properties")
    void hasCorrectProperties() {
        AcademyResearchers card = new AcademyResearchers();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(PutAuraFromHandOntoSelfEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Academy Researchers puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new AcademyResearchers()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Academy Researchers");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Academy Researchers puts it on the battlefield with may prompt, then ETB on stack")
    void resolvingPutsItOnBattlefieldWithEtb() {
        harness.setHand(player1, List.of(new AcademyResearchers()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may on stack

        GameData gd = harness.getGameData();

        // Academy Researchers is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Academy Researchers"));

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // May ability prompt is pending
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);
    }

    // ===== ETB resolution with Auras in hand =====

    @Test
    @DisplayName("ETB prompts controller to choose an Aura from hand")
    void etbPromptsAuraChoice() {
        setupAndCast();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.TARGETED_CARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Only Aura card indices are offered when hand has mixed cards")
    void onlyAuraIndicesOffered() {
        setupAndCast();
        // Hand: [GrizzlyBears, HolyStrength, Pacifism, GrizzlyBears]
        harness.setHand(player1, List.of(new GrizzlyBears(), new HolyStrength(), new Pacifism(), new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.TARGETED_CARD_CHOICE);
        // Only indices 1 (HolyStrength) and 2 (Pacifism) should be valid
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("Choosing an Aura puts it onto the battlefield attached to Academy Researchers")
    void choosingAuraAttachesToSelf() {
        setupAndCast();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        // Choose Holy Strength
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();

        // Holy Strength is on the battlefield
        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Holy Strength"))
                .findFirst()
                .orElse(null);
        assertThat(auraPerm).isNotNull();

        // It's attached to Academy Researchers
        Permanent researchers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Academy Researchers"))
                .findFirst()
                .orElseThrow();
        assertThat(auraPerm.getAttachedTo()).isEqualTo(researchers.getId());

        // Hand is now empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Aura static effect applies to Academy Researchers after attachment")
    void auraStaticEffectApplies() {
        setupAndCast();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();

        Permanent researchers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Academy Researchers"))
                .findFirst()
                .orElseThrow();

        // Holy Strength gives +1/+2, Academy Researchers is 2/2 → should be 3/4
        assertThat(harness.getGameQueryService().getEffectivePower(gd, researchers)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, researchers)).isEqualTo(4);
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining to choose an Aura leaves hand and battlefield unchanged")
    void decliningLeavesHandUnchanged() {
        setupAndCast();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        int battlefieldSizeBefore = harness.getGameData().playerBattlefields.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
        assertThat(gd.interaction.cardChoice()).isNull();
    }

    // ===== No Auras in hand =====

    @Test
    @DisplayName("ETB does nothing when no Auras are in hand")
    void etbDoesNothingWithNoAuras() {
        setupAndCast();
        // Hand has only non-Aura cards
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.TARGETED_CARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("has no Aura cards in hand"));
    }

    @Test
    @DisplayName("ETB does nothing when hand is empty")
    void etbDoesNothingWithEmptyHand() {
        setupAndCast();
        harness.setHand(player1, List.of());
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.TARGETED_CARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("has no Aura cards in hand"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if Academy Researchers left the battlefield before resolution")
    void etbFizzlesIfCreatureLeftBattlefield() {
        setupAndCast();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Remove Academy Researchers from the battlefield before accepting may
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Academy Researchers"));

        harness.handleMayAbilityChosen(player1, true); // accept → inner effect fizzles (source gone)

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.TARGETED_CARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("fizzles"));
    }

    // ===== Multiple Auras =====

    @Test
    @DisplayName("Player can choose among multiple Auras in hand")
    void canChooseAmongMultipleAuras() {
        setupAndCast();
        harness.setHand(player1, List.of(new HolyStrength(), new SpiritLink()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.TARGETED_CARD_CHOICE);
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactlyInAnyOrder(0, 1);

        // Choose Spirit Link (index 1)
        harness.handleCardChosen(player1, 1);

        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spirit Link"))
                .findFirst()
                .orElse(null);
        assertThat(auraPerm).isNotNull();

        Permanent researchers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Academy Researchers"))
                .findFirst()
                .orElseThrow();
        assertThat(auraPerm.getAttachedTo()).isEqualTo(researchers.getId());

        // Holy Strength remains in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Holy Strength");
    }

    // ===== Helper =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new AcademyResearchers()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
    }
}


