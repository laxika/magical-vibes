package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CareeningMineCart.class, ArmoredKincaller.class})
class CareeningMineCartTest extends BaseCardTest {

    @Test
    @DisplayName("Crew 1 animates Careening Mine Cart and taps the crew member")
    void crewAnimatesMineCart() {
        Permanent cart = addCreatureReady(player1, new CareeningMineCart());
        Permanent creature = addCreatureReady(player1, new ArmoredKincaller());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cart)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking with Careening Mine Cart creates a Treasure token")
    void attackCreatesTreasureToken() {
        addCreatureReady(player1, new CareeningMineCart());
        addCreatureReady(player1, new ArmoredKincaller());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void summoningSickCreatureCanCrewWithoutCreatingTreasure() {
        Permanent cart = addCreatureReady(player1, new CareeningMineCart());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());

        harness.activateAbility(player1, 0, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, cart)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cart)).isTrue();
        assertThat(cart.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void cannotCrewWithOnlyOpponentsCreature() {
        Permanent cart = addCreatureReady(player1, new CareeningMineCart());
        Permanent opponentCreature = addCreatureReady(player2, new ArmoredKincaller());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, cart)).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void crewAnimationExpiresAtEndOfTurn() {
        Permanent cart = addCreatureReady(player1, new CareeningMineCart());
        addCreatureReady(player1, new ArmoredKincaller());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, cart)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cart)).isFalse();
    }

    @Test
    void attackTriggerSurvivesMineCartLeavingBattlefield() {
        Permanent cart = addCreatureReady(player1, new CareeningMineCart());
        addCreatureReady(player1, new ArmoredKincaller());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        gd.playerBattlefields.get(player1.getId()).remove(cart);
        gd.playerGraveyards.get(player1.getId()).add(cart.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure").getFirst().isTapped()).isFalse();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }
}