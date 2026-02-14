package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.v.VoiceOfAll;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindBendTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Mind Bend has correct card properties")
    void hasCorrectProperties() {
        MindBend card = new MindBend();

        assertThat(card.getName()).isEqualTo("Mind Bend");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ChangeColorTextEffect.class);
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
        assertThat(entry.getCard().getName()).isEqualTo("Mind Bend");
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    // ===== Resolving - Color word replacement =====

    @Test
    @DisplayName("Resolving Mind Bend prompts for first color choice")
    void resolvingPromptsForFirstChoice() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.awaitingColorChoicePlayerId).isEqualTo(player1.getId());
        assertThat(gd.colorChoiceContext).isInstanceOf(ColorChoiceContext.TextChangeFromWord.class);
    }

    @Test
    @DisplayName("First color choice prompts for second color choice")
    void firstChoicePromptsForSecond() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "BLACK");

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.colorChoiceContext).isInstanceOf(ColorChoiceContext.TextChangeToWord.class);
        ColorChoiceContext.TextChangeToWord ctx = (ColorChoiceContext.TextChangeToWord) gd.colorChoiceContext;
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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "BLACK");
        harness.handleColorChosen(player1, "GREEN");

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.colorChoiceContext).isNull();

        Permanent perm = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Paladin en-Vec"))
                .findFirst().orElseThrow();
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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "BLACK");
        harness.handleColorChosen(player1, "GREEN");

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("changes all instances of black to green") && log.contains("Paladin en-Vec"));
    }

    // ===== Resolving - Basic land type replacement =====

    @Test
    @DisplayName("Replacing basic land type adds text replacement to permanent")
    void replacingLandTypeAddsTextReplacement() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "SWAMP");
        harness.handleColorChosen(player1, "FOREST");

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNull();

        Permanent perm = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "ISLAND");

        GameData gd = harness.getGameData();
        ColorChoiceContext.TextChangeToWord ctx = (ColorChoiceContext.TextChangeToWord) gd.colorChoiceContext;
        assertThat(ctx.isColor()).isFalse();
        assertThat(ctx.fromWord()).isEqualTo("ISLAND");
    }

    // ===== chosenColor update =====

    @Test
    @DisplayName("Mind Bend updates chosenColor when from-color matches")
    void updatesChosenColorWhenMatching() {
        harness.addToBattlefield(player2, new VoiceOfAll());
        // Manually set chosen color to simulate Voice of All's ETB
        Permanent voiceOfAll = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Voice of All"))
                .findFirst().orElseThrow();
        voiceOfAll.setChosenColor(CardColor.BLACK);

        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Voice of All");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "BLACK");
        harness.handleColorChosen(player1, "RED");

        assertThat(voiceOfAll.getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Mind Bend does not update chosenColor when from-color does not match")
    void doesNotUpdateChosenColorWhenNotMatching() {
        harness.addToBattlefield(player2, new VoiceOfAll());
        Permanent voiceOfAll = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Voice of All"))
                .findFirst().orElseThrow();
        voiceOfAll.setChosenColor(CardColor.BLACK);

        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Voice of All");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "WHITE");
        harness.handleColorChosen(player1, "GREEN");

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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "RED");
        harness.handleColorChosen(player1, "BLUE");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mind Bend"));
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
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mind Bend"));
    }

    @Test
    @DisplayName("Multiple Mind Bends stack text replacements on the same permanent")
    void multipleReplacementsStack() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // First Mind Bend: change "black" to "green"
        harness.handleColorChosen(player1, "BLACK");
        harness.handleColorChosen(player1, "GREEN");

        // Cast a second Mind Bend
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Second Mind Bend: change "red" to "blue"
        harness.handleColorChosen(player1, "RED");
        harness.handleColorChosen(player1, "BLUE");

        Permanent perm = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Paladin en-Vec"))
                .findFirst().orElseThrow();
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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "RED");
        harness.handleColorChosen(player1, "WHITE");

        Permanent perm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Paladin en-Vec"))
                .findFirst().orElseThrow();
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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleColorChosen(player1, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cannot choose a land type for second choice when first was a color")
    void cannotMixColorAndLandType() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindBend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleColorChosen(player1, "BLACK");

        assertThatThrownBy(() -> harness.handleColorChosen(player1, "FOREST"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
