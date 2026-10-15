package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosRagemutt.class})
class RakdosRagemuttTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new RakdosRagemutt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rakdos Ragemutt");
    }

    @Test
    @DisplayName("Can attack the turn it enters due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent mutt = harness.addToBattlefieldAndReturn(player1, new RakdosRagemutt());
        mutt.setSummoningSick(true);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Gains life equal to combat damage dealt (lifelink)")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent mutt = harness.addToBattlefieldAndReturn(player1, new RakdosRagemutt());
        mutt.setSummoningSick(false);
        mutt.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Both controllers gain life when Ragemutts deal lethal combat damage to each other")
    void lifelinkAppliesToCreatureDamageEvenWhenSourceDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RakdosRagemutt());
        harness.addToBattlefield(player2, new RakdosRagemutt());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertNotOnBattlefield(player1, "Rakdos Ragemutt");
        harness.assertNotOnBattlefield(player2, "Rakdos Ragemutt");
        harness.assertInGraveyard(player1, "Rakdos Ragemutt");
        harness.assertInGraveyard(player2, "Rakdos Ragemutt");
    }
}
