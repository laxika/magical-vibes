package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaringArchaeologistTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB may-return-artifact and historic spell-cast trigger with +1/+1 counter")
    void hasCorrectStructure() {
        DaringArchaeologist card = new DaringArchaeologist();

        // ETB: MayEffect wrapping ReturnCardFromGraveyardEffect
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ReturnCardFromGraveyardEffect.class);

        // Historic trigger: SpellCastTriggerEffect with PutCounterOnSelfEffect
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.spellFilter()).isInstanceOf(CardIsHistoricPredicate.class);
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(PutCounterOnSelfEffect.class);
        PutCounterOnSelfEffect counterEffect = (PutCounterOnSelfEffect) trigger.resolvedEffects().getFirst();
        assertThat(counterEffect.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
    }

    // ===== ETB: return artifact from graveyard =====

    @Test
    @DisplayName("ETB triggers may ability prompt when artifact is in graveyard")
    void etbTriggersMayPrompt() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        castAndResolve();
        harness.passBothPriorities(); // resolve MayEffect from stack

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may ability returns artifact from graveyard to hand")
    void acceptingMayReturnsArtifact() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        castAndAcceptMay();

        // Graveyard choice should be prompted
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Declining may ability does not return artifact")
    void decliningMayDoesNotReturnArtifact() {
        harness.setGraveyard(player1, List.of(new Spellbook()));
        castAndResolve();
        harness.passBothPriorities(); // resolve MayEffect
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("ETB does not offer non-artifact cards from graveyard")
    void etbDoesNotOfferNonArtifact() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        // No artifact in graveyard — no graveyard choice
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("ETB resolves with no effect if graveyard is empty")
    void etbNoEffectEmptyGraveyard() {
        castAndAcceptMay();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    // ===== Historic trigger: +1/+1 counter =====

    @Test
    @DisplayName("Casting an artifact puts a +1/+1 counter on Daring Archaeologist")
    void artifactSpellPutsCounter() {
        harness.addToBattlefield(player1, new DaringArchaeologist());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Spellbook on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Daring Archaeologist"));

        // Resolve triggered ability
        harness.passBothPriorities();

        Permanent archaeologist = findArchaeologist(player1);
        assertThat(archaeologist.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a legendary creature puts a +1/+1 counter on Daring Archaeologist")
    void legendarySpellPutsCounter() {
        harness.addToBattlefield(player1, new DaringArchaeologist());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Daring Archaeologist"));

        // Resolve triggered ability
        harness.passBothPriorities();

        Permanent archaeologist = findArchaeologist(player1);
        assertThat(archaeologist.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-historic spell does not put a counter")
    void nonHistoricDoesNotPutCounter() {
        harness.addToBattlefield(player1, new DaringArchaeologist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Only the creature spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        Permanent archaeologist = findArchaeologist(player1);
        assertThat(archaeologist.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent casting historic spell does not trigger controller's Daring Archaeologist")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new DaringArchaeologist());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        // Only artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Multiple historic spells put multiple counters")
    void multipleHistoricSpellsPutMultipleCounters() {
        harness.addToBattlefield(player1, new DaringArchaeologist());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        // Cast first artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (+1/+1 counter)
        harness.passBothPriorities(); // resolve Spellbook

        // Cast second artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (+1/+1 counter)

        Permanent archaeologist = findArchaeologist(player1);
        assertThat(archaeologist.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    // ===== Helpers =====

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DaringArchaeologist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
    }

    private void castAndAcceptMay() {
        castAndResolve();
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);
    }

    private Permanent findArchaeologist(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Daring Archaeologist"))
                .findFirst().orElse(null);
    }
}
