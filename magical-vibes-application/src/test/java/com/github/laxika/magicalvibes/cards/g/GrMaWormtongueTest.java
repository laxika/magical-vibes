package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        addCreatureReady(player1, new GrizzlyBears());
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
        addCreatureReady(player1, new KondaLordOfEiganjo());

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
        Permanent army = addCreatureReady(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent fodder = addCreatureReady(player1, new KondaLordOfEiganjo());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ORC);
    }

    @Test
    void canTargetItsControllerAndPaysCostsBeforeResolution() {
        Permanent grima = addGrima();
        addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(grima.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    void cannotSacrificeItselfAsTheOnlyCreature() {
        addGrima();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Gríma Wormtongue");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new GrMaWormtongue());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesOneOfMultipleArmiesAndOnlyThatArmyBecomesOrc() {
        addGrima();
        Permanent firstArmy = addCreatureReady(player1, new GrizzlyBears());
        firstArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent secondArmy = addCreatureReady(player1, new GrizzlyBears());
        secondArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent fodder = addCreatureReady(player1, new KondaLordOfEiganjo());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(secondArmy.getId()));

        assertThat(firstArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(firstArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ORC);
        assertThat(secondArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondArmy.getGrantedSubtypes()).contains(CardSubtype.ORC);
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
    }

    @Test
    void nonlegendarySacrificeDoesNotChangeExistingArmy() {
        addGrima();
        Permanent army = addCreatureReady(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(army.getGrantedSubtypes()).doesNotContain(CardSubtype.ORC);
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
    }

    @Test
    void opposingArmyDoesNotPreventCreatingOwnArmy() {
        addGrima();
        Permanent opposingArmy = addCreatureReady(player2, new GrizzlyBears());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        addCreatureReady(player1, new KondaLordOfEiganjo());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Orc Army")).hasSize(1);
        assertThat(findPermanent(player1, "Orc Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ORC);
    }

    @Test
    void lifeGainIsBlockedOnlyWhileGrimaIsOnBattlefield() {
        Permanent grima = addGrima();
        int ownLife = gd.playerLifeTotals.get(player1.getId());
        int opposingLife = gd.playerLifeTotals.get(player2.getId());

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3);
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3);
        });

        harness.assertLife(player1, ownLife + 3);
        harness.assertLife(player2, opposingLife);
        gd.playerBattlefields.get(player1.getId()).remove(grima);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.assertLife(player2, opposingLife + 3);
    }

    private Permanent addGrima() {
        return addCreatureReady(player1, new GrMaWormtongue());
    }
}
