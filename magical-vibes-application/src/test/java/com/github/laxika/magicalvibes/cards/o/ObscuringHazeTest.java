package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObscuringHaze.class, EdgarMarkov.class, GrizzlyBears.class, ProdigalSorcerer.class})
class ObscuringHazeTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free with a commander and prevents opponents' creature damage")
    void freeCastPreventsOpponentsCreatureDamage() {
        addCommanderToBattlefield(player1);
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        harness.setHand(player1, List.of(new ObscuringHaze()));
        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        resolveCombat(player2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null,
                player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from your creatures")
    void doesNotPreventDamageFromYourCreatures() {
        addCommanderToBattlefield(player1);
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ObscuringHaze()));
        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Cannot use the free alternate cost without controlling a commander")
    void freeCastRequiresCommander() {
        harness.setHand(player1, List.of(new ObscuringHaze()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCommanderToBattlefield(Player player) {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player.getId(), commander);
        harness.addToBattlefield(player, commander);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }
}
