package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiltspireAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's creature that dealt noncombat damage to you this turn")
    void destroysCreatureThatDealtNoncombatDamage() {
        harness.setLife(player1, 20);
        Permanent giltspire = addCreatureReady(player1, new GiltspireAvenger());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        // Opponent's Prodigal Sorcerer pings you — records noncombat damage from the sorcerer to you.
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);

        // {T}: destroy that creature.
        harness.activateAbility(player1, indexOf(player1, giltspire), null, sorcerer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sorcerer);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(sorcerer.getCard().getId()));
        assertThat(giltspire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Destroys a creature that dealt combat damage to you this turn")
    void destroysCreatureThatDealtCombatDamage() {
        Permanent giltspire = addCreatureReady(player1, new GiltspireAvenger());
        Permanent attacker = addCreatureReady(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        // Simulate the attacker having dealt combat damage to you this turn.
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(attacker.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());

        harness.activateAbility(player1, indexOf(player1, giltspire), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("Cannot target a creature that dealt no damage this turn")
    void cannotTargetCreatureThatDealtNoDamage() {
        Permanent giltspire = addCreatureReady(player1, new GiltspireAvenger());
        Permanent bears = addCreatureReady(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, giltspire), null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that dealt damage only to an opponent, not to you")
    void cannotTargetCreatureThatDamagedSomeoneElse() {
        Permanent giltspire = addCreatureReady(player1, new GiltspireAvenger());
        Permanent bears = addCreatureReady(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        // The creature dealt damage to player2, not to Giltspire's controller.
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(bears.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player2.getId());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, giltspire), null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exalted — an ally attacking alone gets +1/+1")
    void exaltedBoostsLoneAttacker() {
        addCreatureReady(player1, new GiltspireAvenger());
        Permanent bears = addCreatureReady(player1, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1)); // Grizzly Bears attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
