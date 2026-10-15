package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngrathCaptainOfChaos.class, GrizzlyBears.class, ArborealGrazer.class, SarkhanTheMasterless.class})
class AngrathCaptainOfChaosTest extends BaseCardTest {

    @Test
    void grantsMenaceToCreaturesYouControl() {
        addReadyAngrath(player1, 5);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void amassesWithoutAnArmy() {
        Permanent angrath = addReadyAngrath(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(angrath.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getEffectivePower()).isEqualTo(2);
        assertThat(army.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void amassesOnAnExistingArmyAndMakesItZombie() {
        Permanent angrath = addReadyAngrath(player1, 5);
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(angrath.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    void choosesOnlyOneArmyAndMakesOnlyThatArmyZombie() {
        addReadyAngrath(player1, 5);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArborealGrazer());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArborealGrazer());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void opposingArmyDoesNotPreventCreatingYourOwnArmy() {
        addReadyAngrath(player1, 5);
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new ArborealGrazer());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, army, Keyword.MENACE)).isTrue();
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
    }

    @Test
    void loyaltyAbilityResolvesAfterAngrathDiesAndMenaceEnds() {
        Permanent angrath = addReadyAngrath(player1, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArborealGrazer());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(angrath);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, army, Keyword.MENACE)).isFalse();
    }

    @Test
    void animatedAngrathHasMenaceAlongWithOtherControlledCreatures() {
        Permanent angrath = addReadyAngrath(player1, 5);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, angrath)).isTrue();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angrath, Keyword.MENACE)).isTrue();
    }

    private Permanent addReadyAngrath(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new AngrathCaptainOfChaos());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
