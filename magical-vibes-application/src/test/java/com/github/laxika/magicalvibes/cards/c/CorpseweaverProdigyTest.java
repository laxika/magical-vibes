package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BridgeFromBelow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoneMissionary;
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
        LoneMissionary.class, Shock.class})
class CorpseweaverProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's creature instead of putting it into a graveyard")
    void exilesOpponentCreatureInsteadOfDying() {
        harness.addToBattlefield(player1, new CorpseweaverProdigy());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
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

    private void enterPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
