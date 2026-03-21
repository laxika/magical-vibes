package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MishraSelfReplicatorTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has historic spell-cast trigger with may pay and CreateTokenCopyOfSourceEffect")
    void hasCorrectStructure() {
        MishraSelfReplicator card = new MishraSelfReplicator();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) mayEffect.wrapped();
        assertThat(trigger.spellFilter()).isInstanceOf(CardIsHistoricPredicate.class);
        assertThat(trigger.manaCost()).isEqualTo("{1}");
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(CreateTokenCopyOfSourceEffect.class);
    }

    // ===== Trigger: casting historic spells =====

    @Test
    @DisplayName("Casting an artifact triggers may ability prompt")
    void castingArtifactTriggersMayPrompt() {
        harness.addToBattlefield(player1, new MishraSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Casting a legendary creature triggers may ability prompt")
    void castingLegendaryTriggersMayPrompt() {
        harness.addToBattlefield(player1, new MishraSelfReplicator());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Casting a non-historic spell does not trigger")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new MishraSelfReplicator());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Only the creature spell on the stack, no may prompt
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Opponent casting historic spell does not trigger controller's Self-Replicator")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new MishraSelfReplicator());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        // Only artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Token creation =====

    @Test
    @DisplayName("Accepting and paying {1} creates a token copy of Self-Replicator")
    void acceptingMayCreatesTokenCopy() {
        harness.addToBattlefield(player1, new MishraSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Mishra's Self-Replicator"));

        // Resolve triggered ability
        harness.passBothPriorities();

        Permanent token = findToken(player1);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getName()).isEqualTo("Mishra's Self-Replicator");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may ability does not create a token")
    void decliningMayDoesNotCreateToken() {
        harness.addToBattlefield(player1, new MishraSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on the stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Mishra's Self-Replicator"));

        assertThat(findToken(player1)).isNull();
    }

    @Test
    @DisplayName("Token copy has the same triggered ability as the original")
    void tokenCopyHasSameAbility() {
        harness.addToBattlefield(player1, new MishraSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Cast first artifact and accept may to create a token
        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve triggered ability (creates token)
        harness.passBothPriorities(); // resolve Spellbook

        Permanent token = findToken(player1);
        assertThat(token).isNotNull();

        // Token should have the same triggered ability
        assertThat(token.getCard().getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(token.getCard().getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
    }

    @Test
    @DisplayName("Source leaving battlefield before resolution creates no token")
    void sourceLeftBattlefieldNoToken() {
        harness.addToBattlefield(player1, new MishraSelfReplicator());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        // Remove the source before ability resolves
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Mishra's Self-Replicator") && !p.getCard().isToken());

        harness.passBothPriorities(); // resolve triggered ability

        // No token should be created since source left the battlefield
        assertThat(findToken(player1)).isNull();
    }

    // ===== Helpers =====

    private Permanent findToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mishra's Self-Replicator") && p.getCard().isToken())
                .findFirst().orElse(null);
    }
}
