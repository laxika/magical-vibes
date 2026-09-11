package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BrilliantHalo;
import com.github.laxika.magicalvibes.cards.l.LingeringMirage;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcademyResearchers.class, ArgothianSwine.class, BrilliantHalo.class,
        LingeringMirage.class, Pacifism.class})
class AcademyResearchersTest extends BaseCardTest {

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
        harness.assertOnBattlefield(player1, "Academy Researchers");

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // May ability prompt is pending
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);
    }

    // ===== ETB resolution with Auras in hand =====

    @Test
    @DisplayName("ETB prompts controller to choose an Aura from hand")
    void etbPromptsAuraChoice() {
        setupAndCast();
        harness.setHand(player1, List.of(new BrilliantHalo()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Only Aura card indices are offered when hand has mixed cards")
    void onlyAuraIndicesOffered() {
        setupAndCast();
        // Hand: [ArgothianSwine, BrilliantHalo, Pacifism, ArgothianSwine]
        harness.setHand(player1, List.of(new ArgothianSwine(), new BrilliantHalo(), new Pacifism(), new ArgothianSwine()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        // Only indices 1 (Brilliant Halo) and 2 (Pacifism) should be valid
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("Choosing an Aura puts it onto the battlefield attached to Academy Researchers")
    void choosingAuraAttachesToSelf() {
        setupAndCast();
        harness.setHand(player1, List.of(new BrilliantHalo()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        // Choose Brilliant Halo
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();

        // Brilliant Halo is on the battlefield
        Permanent auraPerm = findPermanent(player1, "Brilliant Halo");

        // It's attached to Academy Researchers
        Permanent researchers = findPermanent(player1, "Academy Researchers");
        assertThat(auraPerm.getAttachedTo()).isEqualTo(researchers.getId());

        // Hand is now empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Aura static effect applies to Academy Researchers after attachment")
    void auraStaticEffectApplies() {
        setupAndCast();
        harness.setHand(player1, List.of(new BrilliantHalo()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();

        Permanent researchers = findPermanent(player1, "Academy Researchers");

        // Brilliant Halo gives +1/+2, Academy Researchers is 2/2 → should be 3/4
        assertThat(harness.getGameQueryService().getEffectivePower(gd, researchers)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, researchers)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the may ability leaves hand and battlefield unchanged")
    void decliningMayLeavesHandUnchanged() {
        setupAndCast();
        harness.setHand(player1, List.of(new BrilliantHalo()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        int battlefieldSizeBefore = harness.getGameData().playerBattlefields.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the may ability requires choosing an Aura")
    void acceptingMayRequiresAuraSelection() {
        setupAndCast();
        harness.setHand(player1, List.of(new BrilliantHalo()));
        harness.passBothPriorities(); // resolve creature spell - may on stack
        harness.passBothPriorities(); // resolve MayEffect - may prompt
        harness.handleMayAbilityChosen(player1, true); // accept - Aura choice is required

        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== No Auras in hand =====

    @Test
    @DisplayName("ETB does nothing when no Auras are in hand")
    void etbDoesNothingWithNoAuras() {
        setupAndCast();
        // Hand has only non-Aura cards
        harness.setHand(player1, List.of(new ArgothianSwine()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetedHandCardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("has no Aura cards in hand"));
    }

    @Test
    @DisplayName("Only Auras that can enchant Academy Researchers are offered")
    void onlyLegallyEnchantableAurasAreOffered() {
        setupAndCast();
        harness.setHand(player1, List.of(new LingeringMirage(), new Pacifism()));
        harness.passBothPriorities(); // resolve creature spell - may on stack
        harness.passBothPriorities(); // resolve MayEffect - may prompt
        harness.handleMayAbilityChosen(player1, true); // accept - inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetedHandCardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("has no Aura cards in hand"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if Academy Researchers left the battlefield before resolution")
    void etbFizzlesIfCreatureLeftBattlefield() {
        setupAndCast();
        harness.setHand(player1, List.of(new BrilliantHalo()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Remove Academy Researchers from the battlefield before accepting may
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Academy Researchers"));

        harness.handleMayAbilityChosen(player1, true); // accept → inner effect fizzles (source gone)

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetedHandCardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("fizzles"));
    }

    // ===== Multiple Auras =====

    @Test
    @DisplayName("Player can choose among multiple Auras in hand")
    void canChooseAmongMultipleAuras() {
        setupAndCast();
        harness.setHand(player1, List.of(new BrilliantHalo(), new Pacifism()));
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactlyInAnyOrder(0, 1);

        // Choose Pacifism (index 1)
        harness.handleCardChosen(player1, 1);

        Permanent auraPerm = findPermanent(player1, "Pacifism");
        assertThat(auraPerm).isNotNull();

        Permanent researchers = findPermanent(player1, "Academy Researchers");
        assertThat(auraPerm.getAttachedTo()).isEqualTo(researchers.getId());

        // Brilliant Halo remains in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Brilliant Halo");
    }

    // ===== Helper =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new AcademyResearchers()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
    }
}

