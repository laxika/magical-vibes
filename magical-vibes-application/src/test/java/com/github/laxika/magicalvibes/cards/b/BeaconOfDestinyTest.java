package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeaconOfDestiny.class, GrizzlyBears.class, ProdigalPyromancer.class})
class BeaconOfDestinyTest extends BaseCardTest {

    @Test
    void redirectsChosenSourcesPlayerDamageToBeacon() {
        Permanent beacon = addReadyPermanent(player1, new BeaconOfDestiny());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        int lifeBefore = gd.getLife(player1.getId());

        activateAndChooseSource(beacon, pyromancer);
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(beacon.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotRedirectChosenSourcesDamageToCreatures() {
        Permanent beacon = addReadyPermanent(player1, new BeaconOfDestiny());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent victim = addReadyPermanent(player2, new GrizzlyBears());

        activateAndChooseSource(beacon, pyromancer);
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        assertThat(beacon.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotRedirectDamageFromAnotherSource() {
        Permanent beacon = addReadyPermanent(player1, new BeaconOfDestiny());
        Permanent chosenSource = addReadyPermanent(player1, new GrizzlyBears());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        int lifeBefore = gd.getLife(player1.getId());

        activateAndChooseSource(beacon, chosenSource);
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(beacon.getMarkedDamage()).isZero();
    }

    private void activateAndChooseSource(Permanent beacon, Permanent source) {
        harness.activateAbility(player1, indexOf(player1, beacon), 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
