package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CabalPaladinTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Cabal Paladin has historic spell-cast trigger with deal 2 damage to each opponent")
    void hasCorrectStructure() {
        CabalPaladin card = new CabalPaladin();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.spellFilter()).isInstanceOf(CardIsHistoricPredicate.class);

        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(DealDamageToEachOpponentEffect.class);
        DealDamageToEachOpponentEffect damageEffect = (DealDamageToEachOpponentEffect) trigger.resolvedEffects().getFirst();
        assertThat(damageEffect.damage()).isEqualTo(2);
    }

    // ===== Artifact spell triggers =====

    @Test
    @DisplayName("Casting an artifact triggers 2 damage to each opponent")
    void artifactSpellTriggersDamage() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        // Spellbook on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Cabal Paladin"));
    }

    @Test
    @DisplayName("Resolving artifact-triggered ability deals 2 damage to opponent")
    void artifactTriggerDealsDamage() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.setHand(player1, List.of(new Spellbook()));

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);
        // Resolve the triggered ability (LIFO — trigger on top, Spellbook below)
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        // Controller's life should be unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Legendary spell triggers =====

    @Test
    @DisplayName("Casting a legendary creature triggers 2 damage to each opponent")
    void legendarySpellTriggersDamage() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Cabal Paladin"));
    }

    // ===== Non-historic spell does not trigger =====

    @Test
    @DisplayName("Casting a non-historic creature does not trigger damage")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's historic spell does not trigger =====

    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's Cabal Paladin")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new CabalPaladin());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        // Only the artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Multiple historic spells trigger multiple times =====

    @Test
    @DisplayName("Casting two artifact spells triggers damage each time")
    void multipleHistoricSpellsTriggerMultipleTimes() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        // Cast first artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (damage)
        harness.passBothPriorities(); // resolve Spellbook

        // Cast second artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (damage)

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }
}
