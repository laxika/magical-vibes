package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrafRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat melds with owned Midnight Scavengers into Chittering Host")
    void meldsAtBeginningOfCombat() {
        Permanent grafRats = harness.addToBattlefieldAndReturn(player1, new GrafRats());
        Permanent scavengers = harness.addToBattlefieldAndReturn(player1, namedMidnightScavengers());

        advanceToBeginningOfCombat();
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(grafRats.getId()) || p.getId().equals(scavengers.getId()));
        Permanent host = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chittering Host"))
                .findFirst()
                .orElseThrow();
        assertThat(host.getMeldComponentCards()).hasSize(2);
        assertThat(host.getCard()).isInstanceOf(ChitteringHost.class);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Beginning of combat does not trigger without Midnight Scavengers")
    void doesNotTriggerWithoutPartner() {
        harness.addToBattlefield(player1, new GrafRats());

        advanceToBeginningOfCombat();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Graf Rats"));
    }

    @Test
    @DisplayName("Beginning of combat does not trigger when only opponent controls Midnight Scavengers")
    void doesNotTriggerWithOpponentPartner() {
        harness.addToBattlefield(player1, new GrafRats());
        harness.addToBattlefield(player2, namedMidnightScavengers());

        advanceToBeginningOfCombat();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Meld fizzles if Midnight Scavengers leave before resolution")
    void fizzlesIfPartnerLeavesBeforeResolution() {
        Permanent grafRats = harness.addToBattlefieldAndReturn(player1, new GrafRats());
        Permanent scavengers = harness.addToBattlefieldAndReturn(player1, namedMidnightScavengers());

        advanceToBeginningOfCombat();
        assertThat(gd.stack).isNotEmpty();

        gd.playerBattlefields.get(player1.getId()).remove(scavengers);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(grafRats.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Chittering Host"));
    }

    @Test
    @DisplayName("Destroying Chittering Host puts both meld components into the graveyard")
    void destroyingHostPutsBothComponentsInGraveyard() {
        Permanent grafRats = harness.addToBattlefieldAndReturn(player1, new GrafRats());
        Permanent scavengers = harness.addToBattlefieldAndReturn(player1, namedMidnightScavengers());
        Card grafRatsCard = grafRats.getOriginalCard();
        Card scavengersCard = scavengers.getOriginalCard();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        Permanent host = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chittering Host"))
                .findFirst()
                .orElseThrow();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Chittering Host"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(grafRatsCard, scavengersCard);
    }

    @Test
    @DisplayName("Chittering Host ETB gives other creatures +1/+0 and menace until end of turn")
    void hostEtbBoostsAndGrantsMenace() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrafRats());
        harness.addToBattlefield(player1, namedMidnightScavengers());

        advanceToBeginningOfCombat();
        harness.passBothPriorities(); // resolve meld
        harness.passBothPriorities(); // resolve ETB boost + menace

        Permanent host = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chittering Host"))
                .findFirst()
                .orElseThrow();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
        assertThat(host.getPowerModifier()).isEqualTo(0);
    }

    private static Card namedMidnightScavengers() {
        Card partner = new Card();
        partner.setName("Midnight Scavengers");
        partner.setType(CardType.CREATURE);
        partner.setPower(3);
        partner.setToughness(3);
        return partner;
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // BEGINNING_OF_COMBAT — trigger fires onto stack
    }
}
