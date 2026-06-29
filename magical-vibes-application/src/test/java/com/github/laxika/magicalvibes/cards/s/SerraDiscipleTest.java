package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerraDiscipleTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Serra Disciple has historic spell-cast trigger with +1/+1 boost")
    void hasCorrectStructure() {
        SerraDisciple card = new SerraDisciple();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.spellFilter()).isInstanceOf(CardIsHistoricPredicate.class);

        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect boostEffect = (BoostSelfEffect) trigger.resolvedEffects().getFirst();
        assertThat(boostEffect.powerBoost()).isEqualTo(1);
        assertThat(boostEffect.toughnessBoost()).isEqualTo(1);
    }

    // ===== Artifact spell triggers =====

    @Test
    @DisplayName("Casting an artifact triggers +1/+1 boost")
    void artifactSpellTriggersBoost() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Serra Disciple"));
    }

    @Test
    @DisplayName("Resolving artifact-triggered ability gives +1/+1 until end of turn")
    void artifactTriggerBoosts() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve Spellbook

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        Permanent disciple = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Serra Disciple"))
                .findFirst().orElseThrow();
        assertThat(disciple.getPowerModifier()).isEqualTo(1);
        assertThat(disciple.getToughnessModifier()).isEqualTo(1);
    }

    // ===== Legendary spell triggers =====

    @Test
    @DisplayName("Casting a legendary creature triggers +1/+1 boost")
    void legendarySpellTriggersBoost() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Serra Disciple"));
    }

    // ===== Non-historic spell does not trigger =====

    @Test
    @DisplayName("Casting a non-historic creature does not trigger boost")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's historic spell does not trigger =====

    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's Serra Disciple")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new SerraDisciple());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Multiple historic spells stack =====

    @Test
    @DisplayName("Casting two historic spells gives +2/+2 total")
    void multipleHistoricSpellsStack() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        // Cast first artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve Spellbook

        // Cast second artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve Spellbook

        GameData gd = harness.getGameData();
        Permanent disciple = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Serra Disciple"))
                .findFirst().orElseThrow();
        assertThat(disciple.getPowerModifier()).isEqualTo(2);
        assertThat(disciple.getToughnessModifier()).isEqualTo(2);
    }
}
