package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfestingRadroach.class, Millstone.class, GrizzlyBears.class, Forest.class})
class InfestingRadroachTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage gives the damaged player that many rad counters")
    void combatDamageGivesRadCountersEqualToDamage() {
        Permanent roach = addCreatureReady(player1, new InfestingRadroach());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent milling a nonland card may return the roach from the graveyard")
    void opponentMillingNonlandCardReturnsRoachToHand() {
        InfestingRadroach roach = new InfestingRadroach();
        harness.setGraveyard(player1, List.of(roach));
        harness.addToBattlefield(player2, new Millstone());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(roach);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(roach);
    }

    @Test
    @DisplayName("Milling a land does not trigger the graveyard ability")
    void opponentMillingLandDoesNotTrigger() {
        InfestingRadroach roach = new InfestingRadroach();
        harness.setGraveyard(player1, List.of(roach));
        harness.addToBattlefield(player2, new Millstone());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(roach);
    }

    @Test
    @DisplayName("The roach cannot block")
    void cannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new InfestingRadroach());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }
}
