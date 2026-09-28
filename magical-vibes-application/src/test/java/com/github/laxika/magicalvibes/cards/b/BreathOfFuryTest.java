package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
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

@CardUsed({BreathOfFury.class, GrayscaledGharial.class})
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

    private Permanent attachBreath(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BreathOfFury());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void dealCombatDamage() {
        gd.combatPhasesThisTurn = 1;
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.END_OF_COMBAT));
        declareAttackers(List.of(0));
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.BlockerDeclaration) {
            gs.declareBlockers(gd, player2, List.of());
        }
        resolveCombat();
        harness.passBothPriorities();
    }
}
