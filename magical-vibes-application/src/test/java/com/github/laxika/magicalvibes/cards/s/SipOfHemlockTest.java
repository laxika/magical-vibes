package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossusOfAkros;
import com.github.laxika.magicalvibes.cards.r.ReturnedPhalanx;
import com.github.laxika.magicalvibes.cards.v.VilisBrokerOfBlood;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SipOfHemlock.class, ReturnedPhalanx.class, ColossusOfAkros.class, VilisBrokerOfBlood.class})
class SipOfHemlockTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and its controller loses 2 life")
    void destroysCreatureAndControllerLosesLife() {
        harness.addToBattlefield(player2, new ReturnedPhalanx());
        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Returned Phalanx");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Returned Phalanx");
        harness.assertInGraveyard(player2, "Returned Phalanx");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new ReturnedPhalanx());
        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Returned Phalanx");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An indestructible target survives but its controller still loses life")
    void indestructibleCreatureStillCausesLifeLoss() {
        harness.addToBattlefield(player2, new ColossusOfAkros());
        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Colossus of Akros"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Colossus of Akros");
        harness.assertNotInGraveyard(player2, "Colossus of Akros");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Targeting your own creature makes you lose life, not your opponent")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new ReturnedPhalanx());
        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Returned Phalanx"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Returned Phalanx");
        harness.assertInGraveyard(player1, "Returned Phalanx");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Vilis is destroyed before life loss and does not trigger")
    void destroyedCreatureDoesNotTriggerOnSubsequentLifeLoss() {
        harness.addToBattlefield(player2, new VilisBrokerOfBlood());
        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new ReturnedPhalanx(), new ReturnedPhalanx()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Vilis, Broker of Blood"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vilis, Broker of Blood");
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
}
