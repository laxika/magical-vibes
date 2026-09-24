package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrMaWormtongue.class, GrizzlyBears.class, KondaLordOfEiganjo.class})
class GrMaWormtongueTest extends BaseCardTest {

    @Test
    void preventsOpponentsFromGainingLife() {
        addGrima();

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    void sacrificesAnotherCreatureAndMakesTargetPlayerLoseLife() {
        addGrima();
        addReadyCreature(new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
    }

    @Test
    void legendarySacrificeAmassesOrcsWithoutAnArmy() {
        addGrima();
        addReadyCreature(new KondaLordOfEiganjo());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(army.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ORC, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getEffectivePower()).isEqualTo(2);
        assertThat(army.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void legendarySacrificeAmassesOnExistingArmyAndMakesItOrc() {
        addGrima();
        Permanent army = addReadyCreature(new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent fodder = addReadyCreature(new KondaLordOfEiganjo());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ORC);
    }

    private Permanent addGrima() {
        return addReadyCreature(new GrMaWormtongue());
    }

    private Permanent addReadyCreature(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
