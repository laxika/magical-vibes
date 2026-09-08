package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSerpentSociety.class, Shock.class, TyphoidRats.class, GrizzlyBears.class})
class TheSerpentSocietyTest extends BaseCardTest {

    @Test
    @DisplayName("Ward lets the targeted spell resolve when its controller gets five poison counters")
    void wardCanBePaidWithPoisonCounters() {
        addReady(player1, new TheSerpentSociety());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, findPermanent(player1, "The Serpent Society").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(5);
        harness.assertOnBattlefield(player1, "The Serpent Society");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Declining poison-counter ward counters the targeted spell")
    void wardCountersWhenPoisonCountersAreDeclined() {
        addReady(player1, new TheSerpentSociety());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, findPermanent(player1, "The Serpent Society").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("A dying allied deathtouch creature makes each opponent sacrifice a nontoken creature")
    void deathtouchCreatureDeathForcesOpponentSacrifice() {
        addReady(player1, new TheSerpentSociety());
        Permanent rats = addReady(player1, new TyphoidRats());
        addReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, rats.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Typhoid Rats");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A dying allied creature without deathtouch does not trigger the sacrifice ability")
    void nonDeathtouchCreatureDeathDoesNotTrigger() {
        addReady(player1, new TheSerpentSociety());
        Permanent bears = addReady(player1, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        return addCreatureReady(player, card);
    }
}
