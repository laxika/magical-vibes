package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MirranSpyTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Mirran Spy has MayEffect wrapping SpellCastTriggerEffect with artifact filter")
    void hasCorrectStructure() {
        MirranSpy card = new MirranSpy();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) mayEffect.wrapped();
        assertThat(trigger.spellFilter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) trigger.spellFilter()).cardType()).isEqualTo(CardType.ARTIFACT);
    }

    // ===== Trigger fires on artifact cast =====

    @Test
    @DisplayName("Casting an artifact spell triggers may ability prompt")
    void artifactCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new MirranSpy());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Accept: untap target creature =====

    @Test
    @DisplayName("Accepting prompts for target creature and untaps it")
    void acceptUntapsTargetCreature() {
        harness.addToBattlefield(player1, new MirranSpy());
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        // Tap the bears manually
        Permanent bearsPerm = findPermanent(player1, "Grizzly Bears");
        bearsPerm.tap();
        assertThat(bearsPerm.isTapped()).isTrue();

        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        // Should be prompting for target selection
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the tapped creature
        harness.handlePermanentChosen(player1, bearsId);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Mirran Spy"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Bears should now be untapped
        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new MirranSpy());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Tap opponent's creature
        Permanent bearsPerm = findPermanent(player2, "Grizzly Bears");
        bearsPerm.tap();

        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // Choose opponent's creature
        harness.handlePermanentChosen(player1, bearsId);

        // Resolve triggered ability
        harness.passBothPriorities();

        // Opponent's creature should be untapped
        assertThat(bearsPerm.isTapped()).isFalse();
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining may ability does not untap anything")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new MirranSpy());
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Permanent bearsPerm = findPermanent(player1, "Grizzly Bears");
        bearsPerm.tap();

        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Mirran Spy"));

        // Creature still tapped
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    // ===== Non-artifact does not trigger =====

    @Test
    @DisplayName("Non-artifact spell does not trigger Mirran Spy")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new MirranSpy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's artifact does not trigger =====

    @Test
    @DisplayName("Opponent casting artifact does not trigger Mirran Spy")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new MirranSpy());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Permanent not found: " + cardName));
    }
}
