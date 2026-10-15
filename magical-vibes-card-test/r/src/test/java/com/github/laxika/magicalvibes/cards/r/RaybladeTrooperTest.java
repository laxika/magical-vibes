package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaybladeTrooper.class, GrizzlyBears.class, Murder.class})
class RaybladeTrooperTest extends BaseCardTest {

    @Test
    void entersAndPutsCounterOnTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RaybladeTrooper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RaybladeTrooper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void createsHumanSoldierWhenCounteredAllyDies() {
        harness.addToBattlefield(player1, new RaybladeTrooper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithMurder(player2, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    void doesNotCreateHumanSoldierWhenAllyDiesWithoutPlusOneCounter() {
        harness.addToBattlefield(player1, new RaybladeTrooper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithMurder(player2, bears.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    @Test
    void createsHumanSoldierWhenItselfDiesWithPlusOneCounter() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new RaybladeTrooper());
        trooper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithMurder(player2, trooper.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    void canPutEnterCounterOnItself() {
        harness.castFromHand(player1, new RaybladeTrooper(), "{2}{W}");
        harness.passBothPriorities();
        Permanent trooper = findPermanent(player1, "Rayblade Trooper");
        harness.handlePermanentChosen(player1, trooper.getId());
        resolveAllTriggers();

        assertThat(trooper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotCreateTokenWhenItselfDiesWithoutCounter() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new RaybladeTrooper());
        trooper.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Rayblade Trooper");
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    @Test
    void doesNotTriggerForCounteredOpponentsCreature() {
        harness.addToBattlefield(player1, new RaybladeTrooper());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RaybladeTrooper());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponent.setMarkedDamage(3);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Rayblade Trooper");
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Human Soldier")).hasSize(1);
    }

    @Test
    void createsOnlyOneTokenForMultipleCountersAndDoesNotReplaceDyingToken() {
        harness.addToBattlefield(player1, new RaybladeTrooper());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new RaybladeTrooper());
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        ally.setMarkedDamage(5);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(2);
        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        token.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        token.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    void seesOtherCounteredCreatureDieSimultaneously() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RaybladeTrooper());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RaybladeTrooper());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        first.setMarkedDamage(3);
        second.setMarkedDamage(3);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rayblade Trooper")).isEmpty();
        assertThat(findPermanents(player1, "Human Soldier")).hasSize(4);
    }

    @Test
    void warpExilesCounteredTrooperWithoutCreatingDeathToken() {
        RaybladeTrooper card = new RaybladeTrooper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent trooper = findPermanent(player1, "Rayblade Trooper");
        harness.handlePermanentChosen(player1, trooper.getId());
        resolveAllTriggers();

        assertThat(trooper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Rayblade Trooper");
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    @Test
    void warpedTrooperCanBeCastNormallyOnLaterTurnAndStaysInPlay() {
        harness.setLibrary(player1, List.of(new RaybladeTrooper(), new RaybladeTrooper()));
        harness.setLibrary(player2, List.of(new RaybladeTrooper(), new RaybladeTrooper()));
        RaybladeTrooper card = new RaybladeTrooper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, findPermanent(player1, "Rayblade Trooper").getId());
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Rayblade Trooper");
        harness.handlePermanentChosen(player1, returned.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Rayblade Trooper");
    }

    @Test
    void otherCounterTypesDoNotQualifyForDeathTrigger() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new RaybladeTrooper());
        trooper.setCounterCount(CounterType.CHARGE, 1);
        trooper.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Rayblade Trooper");
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    @Test
    void enterTriggerDoesNotPutCounterOnCreatureThatLeftBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RaybladeTrooper());
        harness.setHand(player1, List.of(new RaybladeTrooper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rayblade Trooper")).hasSize(1);
        assertThat(findPermanent(player1, "Rayblade Trooper")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }
    private void destroyWithMurder(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
