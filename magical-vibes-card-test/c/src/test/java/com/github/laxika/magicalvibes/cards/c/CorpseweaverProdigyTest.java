package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BridgeFromBelow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoneMissionary;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseweaverProdigy.class, BridgeFromBelow.class, GrizzlyBears.class,
        LoneMissionary.class, Shock.class, RelentlessAssault.class})
class CorpseweaverProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's creature instead of putting it into a graveyard")
    void exilesOpponentCreatureInsteadOfDying() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears.getCard());
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Conjures Bridge from Below after life gain at the second main phase")
    void conjuresBridgeFromBelowAfterLifeGain() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());

        enterPostcombatMain();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bridge from Below");
    }

    @Test
    @DisplayName("Does not conjure Bridge from Below without life gain")
    void doesNotConjureBridgeFromBelowWithoutLifeGain() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());

        enterPostcombatMain();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Bridge from Below");
    }

    @Test
    void ownCreatureStillDiesNormally() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears.getCard());
    }

    @Test
    void opponentsLifeGainDoesNotEnableInfusion() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        harness.enterBattlefieldAndReturn(player2, new LoneMissionary());

        enterPostcombatMain();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Bridge from Below");
    }

    @Test
    void lifeGainedAfterSecondMainBeginsDoesNotTriggerRetroactively() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        enterPostcombatMain();

        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Bridge from Below");
    }

    @Test
    void lifeLossAfterLifeGainDoesNotPreventInfusion() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player2, 0, player1.getId());
        }

        enterPostcombatMain();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bridge from Below");
    }

    @Test
    void eachProdigyConjuresItsOwnBridge() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());

        enterPostcombatMain();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Bridge from Below"))
                .hasSize(2);
    }

    @Test
    void conjuredBridgeCreatesZombiesAndSurvivesReplacedOpponentDeath() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        Permanent missionary = harness.enterBattlefieldAndReturn(player1, new LoneMissionary());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        enterPostcombatMain();
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, missionary.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zombie");
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Bridge from Below");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears.getCard());
    }

    @Test
    void doesNotConjureAgainDuringThirdMainPhase() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        harness.enterBattlefieldAndReturn(player1, new LoneMissionary());
        enterPostcombatMain();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Bridge from Below");

        harness.setHand(player1, List.of(new RelentlessAssault()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Bridge from Below"))
                .hasSize(1);
    }

    private void enterPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
    }
}
