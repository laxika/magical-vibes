package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.cards.m.MidnightScavengers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrafRats.class, MidnightScavengers.class, ChitteringHost.class, GrizzlyBears.class})
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
        Permanent host = findPermanent(player1, "Chittering Host");
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
        harness.assertOnBattlefield(player1, "Graf Rats");
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
        harness.assertNotOnBattlefield(player1, "Chittering Host");
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

        Permanent host = findPermanent(player1, "Chittering Host");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));

        harness.assertNotOnBattlefield(player1, "Chittering Host");
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

        Permanent host = findPermanent(player1, "Chittering Host");

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
        assertThat(host.getPowerModifier()).isEqualTo(0);
    }

    private static Card namedMidnightScavengers() {
        return new MidnightScavengers();
    }

    @Test
    void tokenPartnerIsExiledButCannotMeld() {
        GrafRats rats = new GrafRats();
        MidnightScavengers token = new MidnightScavengers();
        token.setToken(true);
        harness.addToBattlefield(player1, rats);
        harness.addToBattlefield(player1, token);

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chittering Host");
        harness.assertNotOnBattlefield(player1, "Graf Rats");
        harness.assertNotOnBattlefield(player1, "Midnight Scavengers");
        assertThat(gd.exiledCards).anyMatch(c -> c.card().getId().equals(rats.getId()));
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
