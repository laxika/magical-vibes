package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.s.SeedSpark;
import com.github.laxika.magicalvibes.cards.t.TheMasterMultiplied;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreathOfFury.class, GrayscaledGharial.class, BatheInLight.class,
        CourierHawk.class, SeedSpark.class, TheMasterMultiplied.class})
class BreathOfFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Enchant creature you control rejects an opponent's creature")
    void rejectsOpponentCreatureAsAuraTarget() {
        Permanent opponentCreature = addCreatureReady(player2, new GrayscaledGharial());
        harness.setHand(player1, List.of(new BreathOfFury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifices the enchanted creature, reattaches, untaps creatures, and adds a combat phase")
    void reattachesAndAddsCombatPhase() {
        Permanent attacker = addCreatureReady(player1, new GrayscaledGharial());
        Permanent nextAttacker = addCreatureReady(player1, new GrayscaledGharial());
        nextAttacker.tap();
        Permanent aura = attachBreath(attacker);

        dealCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker).contains(aura, nextAttacker);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grayscaled Gharial"));
        assertThat(aura.getAttachedTo()).isEqualTo(nextAttacker.getId());
        assertThat(nextAttacker.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("With no creature to reattach to, the Aura goes to its owner's graveyard without an extra combat")
    void noCreatureToReattach() {
        Permanent attacker = addCreatureReady(player1, new GrayscaledGharial());
        Permanent aura = attachBreath(attacker);

        dealCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker, aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grayscaled Gharial"))
                .anyMatch(card -> card.getName().equals("Breath of Fury"));
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("The controller chooses among creatures to reattach the Aura to")
    void choosesCreatureToReattachTo() {
        Permanent attacker = addCreatureReady(player1, new GrayscaledGharial());
        Permanent firstChoice = addCreatureReady(player1, new GrayscaledGharial());
        Permanent secondChoice = addCreatureReady(player1, new GrayscaledGharial());
        firstChoice.tap();
        secondChoice.tap();
        Permanent opponentCreature = addCreatureReady(player2, new GrayscaledGharial());
        Permanent aura = attachBreath(attacker);

        dealCombatDamage();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstChoice.getId(), secondChoice.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId(), attacker.getId());

        harness.handlePermanentChosen(player1, secondChoice.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(secondChoice.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker).contains(aura);
        assertThat(firstChoice.isTapped()).isFalse();
        assertThat(secondChoice.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast onto a creature its controller controls")
    void enchantsOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrayscaledGharial());
        harness.setHand(player1, List.of(new BreathOfFury()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castEnchantment(player1, 0, creature.getId());
            harness.passBothPriorities();
        });

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BreathOfFury
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Destroying the Aura in response does not prevent sacrificing the damage dealer")
    void sacrificesCreatureEvenIfAuraWasDestroyed() {
        Permanent attacker = addCreatureReady(player1, new GrayscaledGharial());
        Permanent otherCreature = addCreatureReady(player1, new GrayscaledGharial());
        otherCreature.tap();
        Permanent aura = attachBreath(attacker);
        harness.setHand(player1, List.of(new SeedSpark()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        queueCombatDamageTrigger(attacker);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker, aura);
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("Reattachment excludes creatures with protection from red")
    void reattachesOnlyToCreatureItCanEnchant() {
        Permanent attacker = addCreatureReady(player1, new GrayscaledGharial());
        Permanent legalCreature = addCreatureReady(player1, new GrayscaledGharial());
        Permanent protectedCreature = addCreatureReady(player1, new CourierHawk());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castAndResolveInstant(player1, 0, protectedCreature.getId());
            harness.handleListChoice(player1, "RED");
        });
        Permanent aura = attachBreath(attacker);

        dealCombatDamage();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(aura.getAttachedTo()).isEqualTo(legalCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura).doesNotContain(attacker);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("An unsuccessful sacrifice does not reattach the Aura or grant an extra combat")
    void cannotPaySacrificeWithProtectedCreatureToken() {
        GrayscaledGharial tokenCopy = new GrayscaledGharial();
        tokenCopy.setToken(true);
        Permanent attacker = addCreatureReady(player1, tokenCopy);
        Permanent otherCreature = addCreatureReady(player1, new GrayscaledGharial());
        otherCreature.tap();
        harness.addToBattlefield(player1, new TheMasterMultiplied());
        Permanent aura = attachBreath(attacker);

        dealCombatDamage();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, otherCreature.getId());
        }

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker, aura);
        assertThat(aura.getAttachedTo()).isEqualTo(attacker.getId());
        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    private void queueCombatDamageTrigger(Permanent attacker) {
        gd.combatPhasesThisTurn = 1;
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.COMBAT_DAMAGE, TurnStep.END_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.COMBAT_DAMAGE, TurnStep.END_OF_COMBAT));
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof BreathOfFury);
    }

    private Permanent attachBreath(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BreathOfFury());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void dealCombatDamage() {
        gd.combatPhasesThisTurn = 1;
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.END_OF_COMBAT));
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        harness.passBothPriorities();
    }
}
