package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreathOfFury.class, GrizzlyBears.class})
class BreathOfFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Enchant creature you control rejects an opponent's creature")
    void rejectsOpponentCreatureAsAuraTarget() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new BreathOfFury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifices the enchanted creature, reattaches, untaps creatures, and adds a combat phase")
    void reattachesAndAddsCombatPhase() {
        Permanent attacker = addReadyCreature(player1);
        Permanent nextAttacker = addReadyCreature(player1);
        nextAttacker.tap();
        Permanent aura = attachBreath(attacker);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker).contains(aura, nextAttacker);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(aura.getAttachedTo()).isEqualTo(nextAttacker.getId());
        assertThat(nextAttacker.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("With no creature to reattach to, the Aura goes to its owner's graveyard without an extra combat")
    void noCreatureToReattach() {
        Permanent attacker = addReadyCreature(player1);
        Permanent aura = attachBreath(attacker);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker, aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"))
                .anyMatch(card -> card.getName().equals("Breath of Fury"));
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller chooses among creatures to reattach the Aura to")
    void choosesCreatureToReattachTo() {
        Permanent attacker = addReadyCreature(player1);
        Permanent firstChoice = addReadyCreature(player1);
        Permanent secondChoice = addReadyCreature(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        Permanent aura = attachBreath(attacker);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstChoice.getId(), secondChoice.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId(), attacker.getId());

        harness.handlePermanentChosen(player1, secondChoice.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(secondChoice.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker).contains(aura);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent attachBreath(Permanent creature) {
        Permanent aura = new Permanent(new BreathOfFury());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
