package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.DeadlyCoverUp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HomicideInvestigator.class, NervousGardener.class, Shock.class, DeadlyCoverUp.class})
class HomicideInvestigatorTest extends BaseCardTest {

    @Test
    void investigatesWhenANontokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new NervousGardener());

        killWithShock(player2, player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenATokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        Card token = new NervousGardener();
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        killWithShock(player2, player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenAnOpponentsCreatureDies() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player2, new NervousGardener());

        killWithShock(player1, player2);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void investigatesOnlyOnceForSimultaneousDeaths() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new NervousGardener());
        harness.addToBattlefield(player1, new NervousGardener());

        harness.setHand(player2, List.of(new DeadlyCoverUp()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateAgainLaterInTheSameTurn() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new NervousGardener());
        harness.addToBattlefield(player1, new NervousGardener());

        killWithShock(player2, player1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        UUID remainingGardenerId = harness.getPermanentId(player1, "Nervous Gardener");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, remainingGardenerId);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatesWhenItDiesAlone() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Homicide Investigator"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Homicide Investigator");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void eachInvestigatorInvestigatesIndependently() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new NervousGardener());

        killWithShock(player2, player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void tokenDeathDoesNotUseTheOncePerTurnTrigger() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        Card token = new NervousGardener();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        harness.addToBattlefield(player1, new NervousGardener());

        killWithShock(player2, player1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        killWithShock(player2, player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void canInvestigateAgainOnTheNextPlayersTurn() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new NervousGardener());
        harness.addToBattlefield(player1, new NervousGardener());
        killWithShock(player2, player1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Nervous Gardener"));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void clueCanBeSacrificedToDrawACard() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new NervousGardener());
        killWithShock(player2, player1);
        harness.setLibrary(player1, List.of(new NervousGardener()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        harness.assertInHand(player1, "Nervous Gardener");
    }
    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster,
                               com.github.laxika.magicalvibes.model.Player targetController) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(targetController, "Nervous Gardener");
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
