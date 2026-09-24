package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NYamiClassMotherShip.class, Forest.class, GrizzlyBears.class, SerraAngel.class, Shock.class})
class NYamiClassMotherShipTest extends BaseCardTest {

    @Test
    @DisplayName("Crew animates the Vehicle")
    void crewAnimatesVehicle() {
        Permanent ship = addReadyShip();
        Permanent crew = addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ship.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, ship)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Combat damage offers a top permanent for the battlefield")
    void combatDamagePutsTopPermanentOntoBattlefield() {
        Permanent ship = addReadyShip();
        ship.setAnimatedUntilEndOfTurn(true);
        ship.setAnimatedPower(5);
        ship.setAnimatedToughness(7);
        ship.setAttacking(true);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(topCard)).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Declining the top permanent puts it into hand")
    void decliningTopPermanentPutsItIntoHand() {
        Permanent ship = addReadyShip();
        ship.setAnimatedUntilEndOfTurn(true);
        ship.setAnimatedPower(5);
        ship.setAnimatedToughness(7);
        ship.setAttacking(true);
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(findPermanent(topCard)).isNull();
    }

    @Test
    @DisplayName("A nonpermanent top card goes into hand without a may choice")
    void nonpermanentTopCardGoesIntoHand() {
        Permanent ship = addReadyShip();
        ship.setAnimatedUntilEndOfTurn(true);
        ship.setAnimatedPower(5);
        ship.setAnimatedToughness(7);
        ship.setAttacking(true);
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    private Permanent addReadyShip() {
        Permanent ship = harness.addToBattlefieldAndReturn(player1, new NYamiClassMotherShip());
        ship.setSummoningSick(false);
        return ship;
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
