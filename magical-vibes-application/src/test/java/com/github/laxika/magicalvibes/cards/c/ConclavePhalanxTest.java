package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disembowel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConclavePhalanx.class, Disembowel.class})
class ConclavePhalanxTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains life for each creature its controller controls")
    void gainsLifeForEachCreatureControlled() {
        harness.addToBattlefield(player1, new ConclavePhalanx());
        harness.addToBattlefield(player1, new ConclavePhalanx());
        harness.addToBattlefield(player2, new ConclavePhalanx());

        harness.castFromHand(player1, new ConclavePhalanx(), "{4}{W}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Convoke taps a creature to pay the generic part of its cost")
    void convokeTapsCreatureToPayGenericCost() {
        Permanent convokingCreature = harness.addToBattlefieldAndReturn(player1, new ConclavePhalanx());
        harness.setHand(player1, List.of(new ConclavePhalanx()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokingCreature.getId()));

        assertThat(convokingCreature.isTapped()).isTrue();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("A summoning-sick white creature can convoke the white mana requirement")
    void convokePaysWhiteManaWithSummoningSickCreature() {
        Permanent convokingCreature = harness.addToBattlefieldAndReturn(player1, new ConclavePhalanx());
        convokingCreature.setSummoningSick(true);
        harness.setHand(player1, List.of(new ConclavePhalanx()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokingCreature.getId()));
        resolveAllTriggers();

        assertThat(convokingCreature.isTapped()).isTrue();
        harness.assertLife(player1, 22);
        assertThat(countPermanents(player1, "Conclave Phalanx")).isEqualTo(2);
    }

    @Test
    @DisplayName("The life gain counts creatures present when the trigger resolves")
    void countsCreaturesAtResolution() {
        harness.enterBattlefieldAndReturn(player1, new ConclavePhalanx());
        harness.addToBattlefield(player1, new ConclavePhalanx());
        harness.addToBattlefield(player2, new ConclavePhalanx());

        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger resolves for zero life if its only creature is destroyed in response")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent phalanx = harness.enterBattlefieldAndReturn(player1, new ConclavePhalanx());
        harness.setHand(player2, List.of(new Disembowel()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castInstant(player2, 0, 5, phalanx.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Conclave Phalanx");
        harness.assertNotOnBattlefield(player1, "Conclave Phalanx");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
