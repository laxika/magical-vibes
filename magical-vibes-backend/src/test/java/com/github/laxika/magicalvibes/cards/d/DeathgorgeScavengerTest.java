package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathgorgeScavengerTest extends BaseCardTest {

    // ===== Card structure tests =====

    @Test
    @DisplayName("Has MayEffect wrapping ExileGraveyardCardWithConditionalBonusEffect on ETB and Attack slots")
    void hasCorrectEffects() {
        DeathgorgeScavenger card = new DeathgorgeScavenger();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect etbMay = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etbMay.wrapped()).isInstanceOf(ExileGraveyardCardWithConditionalBonusEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect attackMay = (MayEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(attackMay.wrapped()).isInstanceOf(ExileGraveyardCardWithConditionalBonusEffect.class);

        ExileGraveyardCardWithConditionalBonusEffect effect =
                (ExileGraveyardCardWithConditionalBonusEffect) etbMay.wrapped();
        assertThat(effect.creatureLifeGain()).isEqualTo(2);
        assertThat(effect.noncreaturePowerBoost()).isEqualTo(1);
        assertThat(effect.noncreatureToughnessBoost()).isEqualTo(1);
    }

    // ===== ETB trigger tests =====

    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("ETB exiling a creature card gains 2 life")
        void etbExileCreatureGainsLife() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            castDeathgorgeScavenger();
            harness.passBothPriorities(); // resolve creature spell → ETB → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect triggered ability → may prompt

            // Accept may ability — single graveyard target is auto-selected and effect resolves
            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.MAY_ABILITY_CHOICE)).isTrue();
            harness.handleMayAbilityChosen(player1, true);

            // Creature card exiled: gain 2 life
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Grizzly Bears"))).isTrue();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        }

        @Test
        @DisplayName("ETB exiling a noncreature card gives +1/+1 until end of turn")
        void etbExileNoncreatureGivesBoost() {
            Shock shock = new Shock();
            harness.setGraveyard(player1, List.of(shock));

            castDeathgorgeScavenger();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect triggered ability

            // Accept may ability — single target auto-selected
            harness.handleMayAbilityChosen(player1, true);

            // Noncreature card exiled: source gets +1/+1
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Shock"))).isTrue();

            Permanent scavenger = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Deathgorge Scavenger"))
                    .findFirst().orElseThrow();
            assertThat(scavenger.getPowerModifier()).isEqualTo(1);
            assertThat(scavenger.getToughnessModifier()).isEqualTo(1);
        }

        @Test
        @DisplayName("Declining the may ability does not exile anything")
        void etbDeclineMayDoesNothing() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            castDeathgorgeScavenger();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect triggered ability

            // Decline the may ability
            harness.handleMayAbilityChosen(player1, false);

            // Card should still be in graveyard, no life gain
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.getOrDefault(player1.getId(), 20)).isEqualTo(lifeBefore);
        }
    }

    // ===== Attack trigger tests =====

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking and exiling a creature card gains 2 life")
        void attackExileCreatureGainsLife() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            addReadyScavenger(player1);

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            declareAttackers(List.of(0));

            // Resolve MayEffect triggered ability on stack → may prompt
            harness.passBothPriorities();

            // Accept may ability — single target auto-selected
            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.MAY_ABILITY_CHOICE)).isTrue();
            harness.handleMayAbilityChosen(player1, true);

            // Creature card exiled: gain 2 life
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Grizzly Bears"))).isTrue();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        }

        @Test
        @DisplayName("Attacking and exiling a noncreature card gives +1/+1 until end of turn")
        void attackExileNoncreatureGivesBoost() {
            Shock shock = new Shock();
            harness.setGraveyard(player1, List.of(shock));
            Permanent scavenger = addReadyScavenger(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect triggered ability

            // Accept may ability — single target auto-selected
            harness.handleMayAbilityChosen(player1, true);

            // Noncreature card exiled: source gets +1/+1
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
            assertThat(scavenger.getPowerModifier()).isEqualTo(1);
            assertThat(scavenger.getToughnessModifier()).isEqualTo(1);
        }

        @Test
        @DisplayName("Declining the may ability on attack does not exile anything")
        void attackDeclineMayDoesNothing() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            addReadyScavenger(player1);

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect triggered ability

            // Decline the may ability
            harness.handleMayAbilityChosen(player1, false);

            // Card should still be in graveyard, no life gain
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.getOrDefault(player1.getId(), 20)).isEqualTo(lifeBefore);
        }
    }

    // ===== Multi-target graveyard choice (regression: NPE when beginGraveyardChoice lost may-ability context) =====

    @Nested
    @DisplayName("ETB with multiple graveyard targets")
    class ETBMultiTarget {

        @Test
        @DisplayName("Multiple graveyard cards prompt a choice; choosing a creature gains 2 life")
        void etbMultiTargetChooseCreatureGainsLife() {
            GrizzlyBears bears = new GrizzlyBears();
            HillGiant giant = new HillGiant();
            harness.setGraveyard(player1, List.of(bears, giant));

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            castDeathgorgeScavenger();
            harness.passBothPriorities(); // resolve creature spell → ETB → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect triggered ability → may prompt

            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.MAY_ABILITY_CHOICE)).isTrue();
            harness.handleMayAbilityChosen(player1, true);

            // Multiple targets → graveyard choice prompt
            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.GRAVEYARD_CHOICE)).isTrue();
            harness.handleGraveyardCardChosen(player1, 0); // choose Grizzly Bears

            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Grizzly Bears"))).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        }

        @Test
        @DisplayName("Multiple graveyard cards prompt a choice; choosing a noncreature gives +1/+1")
        void etbMultiTargetChooseNoncreatureGivesBoost() {
            GrizzlyBears bears = new GrizzlyBears();
            Shock shock = new Shock();
            harness.setGraveyard(player1, List.of(bears, shock));

            castDeathgorgeScavenger();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve MayEffect triggered ability

            harness.handleMayAbilityChosen(player1, true);

            // Multiple targets → graveyard choice prompt
            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.GRAVEYARD_CHOICE)).isTrue();
            harness.handleGraveyardCardChosen(player1, 1); // choose Shock (noncreature)

            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Shock"))).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

            Permanent scavenger = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Deathgorge Scavenger"))
                    .findFirst().orElseThrow();
            assertThat(scavenger.getPowerModifier()).isEqualTo(1);
            assertThat(scavenger.getToughnessModifier()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Attack with multiple graveyard targets")
    class AttackMultiTarget {

        @Test
        @DisplayName("Attacking with multiple graveyard targets; choosing a creature gains 2 life")
        void attackMultiTargetChooseCreatureGainsLife() {
            GrizzlyBears bears = new GrizzlyBears();
            HillGiant giant = new HillGiant();
            harness.setGraveyard(player1, List.of(bears, giant));
            addReadyScavenger(player1);

            int lifeBefore = gd.playerLifeTotals.getOrDefault(player1.getId(), 20);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect triggered ability → may prompt

            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.MAY_ABILITY_CHOICE)).isTrue();
            harness.handleMayAbilityChosen(player1, true);

            // Multiple targets → graveyard choice prompt
            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.GRAVEYARD_CHOICE)).isTrue();
            harness.handleGraveyardCardChosen(player1, 0); // choose Grizzly Bears

            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Grizzly Bears"))).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        }

        @Test
        @DisplayName("Attacking with multiple graveyard targets; choosing a noncreature gives +1/+1")
        void attackMultiTargetChooseNoncreatureGivesBoost() {
            GrizzlyBears bears = new GrizzlyBears();
            Shock shock = new Shock();
            harness.setGraveyard(player1, List.of(bears, shock));
            Permanent scavenger = addReadyScavenger(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect triggered ability

            harness.handleMayAbilityChosen(player1, true);

            // Multiple targets → graveyard choice prompt
            assertThat(gd.interaction.isAwaitingInput(AwaitingInput.GRAVEYARD_CHOICE)).isTrue();
            harness.handleGraveyardCardChosen(player1, 1); // choose Shock (noncreature)

            assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Shock"))).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(scavenger.getPowerModifier()).isEqualTo(1);
            assertThat(scavenger.getToughnessModifier()).isEqualTo(1);
        }
    }

    // ===== Helpers =====

    private void castDeathgorgeScavenger() {
        harness.setHand(player1, List.of(new DeathgorgeScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadyScavenger(Player player) {
        Permanent perm = new Permanent(new DeathgorgeScavenger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
