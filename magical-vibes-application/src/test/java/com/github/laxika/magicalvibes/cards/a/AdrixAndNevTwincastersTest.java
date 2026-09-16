package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
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

@CardUsed({AdrixAndNevTwincasters.class, BladeSplicer.class, Shock.class})
class AdrixAndNevTwincastersTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles tokens created under its controller's control")
    void doublesTokens() {
        harness.addToBattlefield(player1, new AdrixAndNevTwincasters());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(2);
    }

    @Test
    @DisplayName("Does not double tokens created under an opponent's control")
    void doesNotDoubleOpponentTokens() {
        harness.addToBattlefield(player1, new AdrixAndNevTwincasters());
        harness.setHand(player2, List.of(new BladeSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Phyrexian Golem")).hasSize(1);
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they do not pay")
    void wardCountersUnpaidSpell() {
        Permanent adrixAndNev = harness.addToBattlefieldAndReturn(player1, new AdrixAndNevTwincasters());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, adrixAndNev.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Adrix and Nev, Twincasters");
    }

    @Test
    @DisplayName("Ward {2} lets an opponent's spell resolve when they pay")
    void wardCanBePaid() {
        Permanent adrixAndNev = harness.addToBattlefieldAndReturn(player1, new AdrixAndNevTwincasters());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, adrixAndNev.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(adrixAndNev.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }
}
