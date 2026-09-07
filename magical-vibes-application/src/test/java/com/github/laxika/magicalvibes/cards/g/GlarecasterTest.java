package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glarecaster.class, GrizzlyBears.class, ProdigalPyromancer.class})
class GlarecasterTest extends BaseCardTest {

    @Test
    void redirectsTheNextDamageToTheCreature() {
        Permanent glarecaster = addReady(player1, new Glarecaster());
        Permanent destination = addReady(player1, new GrizzlyBears());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());

        activateGlarecaster(glarecaster, destination);
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, glarecaster.getId());
        harness.passBothPriorities();

        assertThat(glarecaster.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void redirectsTheNextDamageToTheController() {
        Permanent glarecaster = addReady(player1, new Glarecaster());
        Permanent destination = addReady(player1, new GrizzlyBears());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        int lifeBefore = gd.getLife(player1.getId());

        activateGlarecaster(glarecaster, destination);
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void sharedShieldIsConsumedByTheFirstProtectedRecipient() {
        Permanent glarecaster = addReady(player1, new Glarecaster());
        Permanent destination = addReady(player1, new GrizzlyBears());
        Permanent firstPyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent secondPyromancer = addReady(player1, new ProdigalPyromancer());
        int lifeBefore = gd.getLife(player1.getId());

        activateGlarecaster(glarecaster, destination);
        harness.activateAbility(player1, indexOf(player1, firstPyromancer), null, glarecaster.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondPyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(glarecaster.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    private void activateGlarecaster(Permanent glarecaster, Permanent destination) {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, glarecaster), null, destination.getId());
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
