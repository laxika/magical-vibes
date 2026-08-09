package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LancersEnKorTest extends BaseCardTest {

    @Test
    @DisplayName("The free ability redirects damage to a creature you control")
    void redirectsDamageToControlledCreature() {
        Permanent lancers = addReadyPermanent(player1, new LancersEnKor());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent destination = addReadyStats(player1, 3, 3);

        harness.activateAbility(player1, indexOf(player1, lancers), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, lancers.getId());
        harness.passBothPriorities();

        assertThat(lancers.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the next 1 damage is redirected")
    void redirectsOnlyOneDamage() {
        Permanent lancers = addReadyPermanent(player1, new LancersEnKor());
        Permanent firstPyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent secondPyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent destination = addReadyStats(player1, 3, 3);

        harness.activateAbility(player1, indexOf(player1, lancers), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, firstPyromancer), null, lancers.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondPyromancer), null, lancers.getId());
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(lancers.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent lancers = addReadyPermanent(player1, new LancersEnKor());
        Permanent opponentCreature = addReadyStats(player2, 3, 3);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, lancers), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addReadyPermanent(player, card);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
