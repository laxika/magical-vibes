package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralsRegaliaTest extends BaseCardTest {

    @Test
    void redirectsNextDamageFromChosenSourceToControlledCreature() {
        Permanent regalia = addReadyPermanent(player1, new GeneralsRegalia());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent creature = addReadyCreature(player1, 3, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);

        pyromancer.untap();
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void onlyRedirectsDamageFromTheChosenSource() {
        Permanent regalia = addReadyPermanent(player1, new GeneralsRegalia());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent otherSource = addReadyCreature(player1, 2, 2);
        Permanent creature = addReadyCreature(player1, 3, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, regalia), null, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherSource.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addReadyPermanent(player, card);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
