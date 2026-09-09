package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.c.Convalescence;
import com.github.laxika.magicalvibes.cards.e.ElvishBerserker;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({JackalopeHerd.class, ElvishBerserker.class})
class JackalopeHerdTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell returns Jackalope Herd to its owner's hand")
    void ownSpellReturnsJackalopeHerdToHand() {
        harness.addToBattlefield(player1, new JackalopeHerd());
        harness.castFromHand(player1, new ElvishBerserker(), "{G}");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jackalope Herd");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Elvish Berserker");
    }

    @Test
    @DisplayName("An opponent's spell does not return Jackalope Herd")
    void opponentSpellDoesNotReturnJackalopeHerd() {
        harness.addToBattlefield(player1, new JackalopeHerd());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ElvishBerserker(), "{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jackalope Herd");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Elvish Berserker");
    }

    @CardUsed({Convalescence.class})
    @Test
    @DisplayName("Casting a noncreature spell returns Jackalope Herd to its owner's hand")
    void noncreatureSpellReturnsJackalopeHerdToHand() {
        harness.addToBattlefield(player1, new JackalopeHerd());
        harness.castFromHand(player1, new Convalescence(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jackalope Herd");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Convalescence");
    }

    @CardUsed({CityOfTraitors.class})
    @Test
    @DisplayName("Playing a land does not return Jackalope Herd")
    void playingLandDoesNotReturnJackalopeHerd() {
        harness.addToBattlefield(player1, new JackalopeHerd());
        harness.setHand(player1, List.of(new CityOfTraitors()));

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Jackalope Herd");
        harness.assertOnBattlefield(player1, "City of Traitors");
    }

    @Test
    @DisplayName("Returns an opponent-controlled Jackalope Herd to its owner's hand")
    void returnsOpponentControlledJackalopeHerdToOwnerHand() {
        JackalopeHerd herd = new JackalopeHerd();
        herd.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, herd);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ElvishBerserker(), "{G}");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jackalope Herd");
        harness.assertNotInHand(player2, "Jackalope Herd");
    }
}
