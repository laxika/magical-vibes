package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BattlewiseAven;
import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.i.InvasionOfTolvada;
import com.github.laxika.magicalvibes.cards.t.TheBrokenSky;
import com.github.laxika.magicalvibes.cards.w.WrennAndRealmbreaker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlewiseAven.class, EmberShot.class, InvasionOfTolvada.class, ShieldmageAdvocate.class,
        TheBrokenSky.class, WrennAndRealmbreaker.class})
class ShieldmageAdvocateTest extends BaseCardTest {

    @Test
    void returnsOpponentsGraveyardCardAndPreventsAllDamageToPlayer() {
        Permanent advocate = addCreatureReady(player1, new ShieldmageAdvocate());
        Card returnedCard = new BattlewiseAven();
        Permanent source = addReadyCreatureWithStats(player2, 5, 5);
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.setLife(player1, 20);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(returnedCard.getId(), player1.getId()));
        assertThat(advocate.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card.getId().equals(returnedCard.getId()));

        source.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void preventsAllDamageToTargetCreature() {
        Permanent target = addCreatureReady(player1, new BattlewiseAven());
        addCreatureReady(player1, new ShieldmageAdvocate());
        Card returnedCard = new BattlewiseAven();
        Permanent source = addReadyCreatureWithStats(player2, 3, 3);
        harness.setGraveyard(player2, List.of(returnedCard));

        harness.activateAbilityWithMultiTargets(player1, 1, 0, List.of(returnedCard.getId(), target.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void preventsDamageOnlyFromChosenSource() {
        addCreatureReady(player1, new ShieldmageAdvocate());
        Card returnedCard = new BattlewiseAven();
        Permanent chosenSource = addReadyCreatureWithStats(player2, 3, 3);
        Permanent otherSource = addReadyCreatureWithStats(player2, 2, 2);
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.setLife(player1, 20);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(returnedCard.getId(), player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        chosenSource.setAttacking(true);
        otherSource.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void preventsNoncombatDamageFromChosenSpellSource() {
        addCreatureReady(player1, new ShieldmageAdvocate());
        Card returnedCard = new BattlewiseAven();
        EmberShot emberShot = new EmberShot();
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.setHand(player2, List.of(emberShot));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(returnedCard.getId(), player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, emberShot.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotTargetCardInControllerGraveyard() {
        addCreatureReady(player1, new ShieldmageAdvocate());
        Card ownCard = new BattlewiseAven();
        harness.setGraveyard(player1, List.of(ownCard));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(ownCard.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void preventsAllDamageToTargetPlaneswalker() {
        addCreatureReady(player1, new ShieldmageAdvocate());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new WrennAndRealmbreaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        Card returnedCard = new BattlewiseAven();
        Permanent source = addReadyCreatureWithStats(player2, 3, 3);
        harness.setGraveyard(player2, List.of(returnedCard));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(returnedCard.getId(), planeswalker.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        source.setAttackTarget(planeswalker.getId());
        resolveCombat(player2);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void preventsAllDamageToTargetBattle() {
        addCreatureReady(player1, new ShieldmageAdvocate());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfTolvada());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Card returnedCard = new BattlewiseAven();
        Permanent source = addReadyCreatureWithStats(player2, 3, 3);
        harness.setGraveyard(player2, List.of(returnedCard));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(returnedCard.getId(), battle.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        source.setAttackTarget(battle.getId());
        resolveCombat(player2);

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
    }

    @Test
    void preventionStillResolvesWhenGraveyardTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new ShieldmageAdvocate());
        Card returnedCard = new BattlewiseAven();
        Permanent source = addReadyCreatureWithStats(player2, 3, 3);
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.setLife(player1, 20);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(returnedCard.getId(), player1.getId()));
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.playerHands.get(player2.getId())).noneMatch(card -> card.getId().equals(returnedCard.getId()));

        source.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private Permanent addReadyCreatureWithStats(Player player, int power, int toughness) {
        BattlewiseAven card = new BattlewiseAven();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
