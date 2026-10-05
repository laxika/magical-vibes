package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonsClaw;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.v.VoiceOfAll;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindBend.class, GrizzlyBears.class, PaladinEnVec.class, VoiceOfAll.class,
        DragonsClaw.class, Swamp.class})
class MindBendTest extends BaseCardTest {

    @Test
    @DisplayName("Successive color changes rewrite the protection created by earlier text changes")
    void successiveChangesRewriteProtection() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, paladin.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isTrue();

        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, paladin.getId());
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, paladin, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Changing a basic land type changes its intrinsic mana ability")
    void changingBasicLandTypeChangesManaAbility() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, swamp.getId());
        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "FOREST");

        assertThat(gqs.effectiveBasicLandTypes(gd, swamp)).containsExactly(CardSubtype.FOREST);
        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Changing a color word rewrites the color checked by a spell-cast trigger")
    void changingColorRewritesSpellCastTrigger() {
        Permanent claw = harness.addToBattlefieldAndReturn(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, claw.getId());
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard() instanceof DragonsClaw);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, lifeBefore + 1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Mind Bend puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    // ===== Resolving - Color word replacement =====

    @Test
    @DisplayName("Resolving Mind Bend prompts for first color choice")
    void resolvingPromptsForFirstChoice() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.TextChangeFromWord.class);
    }

    @Test
    @DisplayName("First color choice prompts for second color choice")
    void firstChoicePromptsForSecond() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()).isInstanceOf(ChoiceContext.TextChangeToWord.class);
        ChoiceContext.TextChangeToWord ctx = (ChoiceContext.TextChangeToWord) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
        assertThat(ctx.fromWord()).isEqualTo("BLACK");
        assertThat(ctx.isColor()).isTrue();
    }

    @Test
    @DisplayName("Replacing color word adds text replacement to permanent")
    void replacingColorWordAddsTextReplacement() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();

        Permanent perm = findPermanent(player2, "Paladin en-Vec");
        assertThat(perm.getTextReplacements()).hasSize(1);
        assertThat(perm.getTextReplacements().getFirst()).isEqualTo(new TextReplacement("black", "green"));
    }

    @Test
    @DisplayName("Replacing color word logs the change")
    void replacingColorWordLogsChange() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("changes all instances of black to green") && log.contains("Paladin en-Vec"));
    }

    // ===== Resolving - Basic land type replacement =====

    @Test
    @DisplayName("Replacing basic land type adds text replacement to permanent")
    void replacingLandTypeAddsTextReplacement() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "FOREST");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();

        Permanent perm = findPermanent(player2, "Grizzly Bears");
        assertThat(perm.getTextReplacements()).hasSize(1);
        assertThat(perm.getTextReplacements().getFirst()).isEqualTo(new TextReplacement("Swamp", "Forest"));
    }

    @Test
    @DisplayName("Land type first choice restricts second choice to land types")
    void landTypeFirstChoiceRestrictsSecondToLandTypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "ISLAND");

        GameData gd = harness.getGameData();
        ChoiceContext.TextChangeToWord ctx = (ChoiceContext.TextChangeToWord) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
        assertThat(ctx.isColor()).isFalse();
        assertThat(ctx.fromWord()).isEqualTo("ISLAND");
    }

    // ===== chosenColor update =====

    @Test
    @DisplayName("Mind Bend does not change a previously chosen color")
    void doesNotChangeChosenColorWhenMatching() {
        harness.addToBattlefield(player2, new VoiceOfAll());
        // Manually set chosen color to simulate Voice of All's ETB
        Permanent voiceOfAll = findPermanent(player2, "Voice of All");
        voiceOfAll.setChosenColor(CardColor.BLACK);

        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Voice of All");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");

        assertThat(voiceOfAll.getChosenColor()).isEqualTo(CardColor.BLACK);
    }

    @Test
    @DisplayName("Mind Bend does not update chosenColor when from-color does not match")
    void doesNotUpdateChosenColorWhenNotMatching() {
        harness.addToBattlefield(player2, new VoiceOfAll());
        Permanent voiceOfAll = findPermanent(player2, "Voice of All");
        voiceOfAll.setChosenColor(CardColor.BLACK);

        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Voice of All");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "GREEN");

        assertThat(voiceOfAll.getChosenColor()).isEqualTo(CardColor.BLACK);
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Mind Bend goes to graveyard after resolving")
    void mindBendGoesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mind Bend");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Mind Bend");
    }

    @Test
    @DisplayName("Multiple Mind Bends stack text replacements on the same permanent")
    void multipleReplacementsStack() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castAndResolveInstant(player1, 0, targetId);

        // First Mind Bend: change "black" to "green"
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        // Cast a second Mind Bend
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        // Second Mind Bend: change "red" to "blue"
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        Permanent perm = findPermanent(player2, "Paladin en-Vec");
        assertThat(perm.getTextReplacements()).hasSize(2);
        assertThat(perm.getTextReplacements().get(0)).isEqualTo(new TextReplacement("black", "green"));
        assertThat(perm.getTextReplacements().get(1)).isEqualTo(new TextReplacement("red", "blue"));
    }

    @Test
    @DisplayName("Can target own permanents")
    void canTargetOwnPermanents() {
        harness.addToBattlefield(player1, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Paladin en-Vec");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "WHITE");

        Permanent perm = findPermanent(player1, "Paladin en-Vec");
        assertThat(perm.getTextReplacements()).hasSize(1);
        assertThat(perm.getTextReplacements().getFirst()).isEqualTo(new TextReplacement("red", "white"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot choose an invalid word for first choice")
    void cannotChooseInvalidFirstChoice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cannot choose a land type for second choice when first was a color")
    void cannotMixColorAndLandType() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "FOREST"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cannot choose the same word for both text-change choices")
    void cannotChooseSameWordTwice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLACK"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

