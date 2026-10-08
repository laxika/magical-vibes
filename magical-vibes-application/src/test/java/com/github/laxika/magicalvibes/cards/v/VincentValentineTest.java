package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GalianBeast;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VincentValentine.class, GalianBeast.class, GrizzlyBears.class, LightningBolt.class})
class VincentValentineTest extends BaseCardTest {

    @Test
    void putsCountersEqualToOpponentCreaturePowerWhenItDies() {
        Permanent vincent = addVincent(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castLightningBolt(player2, bears);

        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackingMayTransformVincent() {
        Permanent vincent = addVincent(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vincent.isTransformed()).isTrue();
    }

    @Test
    void attackingMayDeclineTransformation() {
        Permanent vincent = addVincent(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vincent.isTransformed()).isFalse();
    }

    @Test
    void friendlyCreatureDeathDoesNotAddCounters() {
        Permanent vincent = addVincent(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castLightningBolt(player2, bears);

        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void dyingCreaturePowerIncludesCountersBeforeItLeavesBattlefield() {
        Permanent vincent = addVincent(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castLightningBolt(player2, bears);

        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void transformationRetainsCountersAndGalianBeastGainsLifeFromCombatDamage() {
        Permanent vincent = addVincent(player1);
        vincent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat();

        assertThat(vincent.isTransformed()).isTrue();
        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    @Test
    void galianBeastDoesNotGainCountersWhenOpponentCreatureDies() {
        Permanent galian = addTransformedVincent(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLightningBolt(player2, bears);

        assertThat(galian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void frontFaceDoesNotReturnWhenItDies() {
        Permanent vincent = addVincent(player1);

        castLightningBolt(player2, vincent);

        harness.assertNotOnBattlefield(player1, "Vincent Valentine");
        harness.assertInGraveyard(player1, "Vincent Valentine");
    }

    @Test
    void galianBeastTramplesAndGainsLifeEvenWhenCombatKillsIt() {
        addTransformedVincent(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 10);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 1));
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent returned = findPermanent(player1, "Vincent Valentine");
        assertThat(returned.isTransformed()).isFalse();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void galianBeastReturnsAsTappedVincentWhenItDies() {
        Permanent galian = addTransformedVincent(player1);
        Card originalCard = galian.getOriginalCard();
        castLightningBolt(player2, galian);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(originalCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTransformed()).isFalse();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(originalCard.getId()));
    }

    private Permanent addVincent(Player player) {
        return addCreatureReady(player, new VincentValentine());
    }

    private Permanent addTransformedVincent(Player player) {
        Card front = new VincentValentine();
        Permanent permanent = new Permanent(front);
        permanent.setCard(front.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castLightningBolt(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new LightningBolt()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        resolveAllTriggers();
    }
}
