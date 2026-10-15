package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.ScattershotArcher;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhoxMeditant.class, ScattershotArcher.class, Unsummon.class})
class RhoxMeditantTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card on enter when controlling a green permanent")
    void drawsWhenControllingGreenPermanent() {
        harness.addToBattlefield(player1, new ScattershotArcher()); // green permanent
        harness.setHand(player1, List.of(new RhoxMeditant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Rhox Meditant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1); // drew a card
    }

    @Test
    @DisplayName("Does not draw when controlling no green permanent")
    void noDrawWithoutGreenPermanent() {
        harness.setHand(player1, List.of(new RhoxMeditant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        assertThat(gd.stack).isEmpty(); // the condition prevents the ability from triggering

        harness.assertOnBattlefield(player1, "Rhox Meditant");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty(); // no draw
    }

    @Test
    @DisplayName("Does not draw when only the opponent controls a green permanent")
    void noDrawWhenOpponentControlsGreenPermanent() {
        harness.addToBattlefield(player2, new ScattershotArcher()); // opponent's green permanent
        harness.setHand(player1, List.of(new RhoxMeditant()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        assertThat(gd.stack).isEmpty(); // the condition prevents the ability from triggering

        assertThat(gd.playerHands.get(player1.getId())).isEmpty(); // "you control" fails
    }

    @Test
    @DisplayName("Does not draw if the only green permanent leaves before resolution")
    void rechecksGreenPermanentOnResolution() {
        harness.addToBattlefield(player1, new ScattershotArcher());
        harness.setHand(player1, List.of(new RhoxMeditant()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Scattershot Archer"));
        harness.assertNotOnBattlefield(player1, "Scattershot Archer");
        int handSizeBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeTrigger);
    }

    @Test
    @DisplayName("The ability still draws if Rhox Meditant leaves before resolution")
    void drawsAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new ScattershotArcher());
        harness.setHand(player1, List.of(new RhoxMeditant()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Rhox Meditant"));
        harness.assertNotOnBattlefield(player1, "Rhox Meditant");
        int handSizeBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeTrigger + 1);
    }
}
