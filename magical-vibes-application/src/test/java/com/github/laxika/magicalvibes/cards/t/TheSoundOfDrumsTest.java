package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheSoundOfDrums.class, GrizzlyBears.class, ProdigalSorcerer.class, SerraAngel.class})
class TheSoundOfDrumsTest extends BaseCardTest {

    @Test
    @DisplayName("Goads the enchanted creature")
    void goadsEnchantedCreature() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachAura(player1, creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Doubles the enchanted creature's combat damage to a player")
    void doublesCombatDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachAura(player1, creature);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles the enchanted creature's combat damage to a permanent")
    void doublesCombatDamageToPermanent() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attachAura(player1, attacker);
        Permanent blocker = addReadyCreature(player2, new SerraAngel());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Does not double noncombat damage from the enchanted creature")
    void doesNotDoubleNoncombatDamage() {
        harness.setLife(player2, 20);
        Permanent sorcerer = addReadyCreature(player1, new ProdigalSorcerer());
        attachAura(player1, sorcerer);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(sorcerer), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Returns itself from the graveyard to its owner's hand")
    void returnsFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new TheSoundOfDrums()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "The Sound of Drums");
        harness.assertNotInGraveyard(player1, "The Sound of Drums");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new TheSoundOfDrums());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
