package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThwartTheEnemy.class, GrizzlyBears.class, ProdigalSorcerer.class, Shock.class})
class ThwartTheEnemyTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat and noncombat damage from opponents' creatures")
    void preventsDamageFromOpponentsCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        castThwartTheEnemy();

        resolveCombat(player2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null,
                player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Does not prevent damage from your creatures")
    void doesNotPreventDamageFromYourCreatures() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, new GrizzlyBears());

        castThwartTheEnemy();
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Does not prevent damage from noncreature sources")
    void doesNotPreventDamageFromNoncreatureSources() {
        harness.setLife(player1, 20);
        castThwartTheEnemy();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }

    private void castThwartTheEnemy() {
        harness.setHand(player1, List.of(new ThwartTheEnemy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
