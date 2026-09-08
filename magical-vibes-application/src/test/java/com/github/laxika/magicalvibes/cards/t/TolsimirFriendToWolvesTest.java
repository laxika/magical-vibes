package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolsimirFriendToWolves.class, GrizzlyBears.class, HillGiant.class})
class TolsimirFriendToWolvesTest extends BaseCardTest {

    @Test
    void enteringCreatesLegendaryVojaAndGainsLife() {
        castTolsimir();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent voja = findPermanent(player1, "Voja, Friend to Elves");
        assertThat(voja.getCard().isToken()).isTrue();
        assertThat(voja.getCard().getPower()).isEqualTo(3);
        assertThat(voja.getCard().getToughness()).isEqualTo(3);
        assertThat(voja.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(voja.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(voja.getCard().getSupertypes()).containsExactly(CardSupertype.LEGENDARY);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void wolfEntryGainsLifeAndFightsOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castTolsimir();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Voja, Friend to Elves");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void nonWolfEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new TolsimirFriendToWolves());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void castTolsimir() {
        harness.setHand(player1, List.of(new TolsimirFriendToWolves()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
