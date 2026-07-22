package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MorbidOpportunistTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private void seedLibrary(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }
    }

    @Test
    @DisplayName("Draws a card when another creature dies")
    void drawsWhenOtherCreatureDies() {
        harness.addToBattlefield(player1, new MorbidOpportunist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve Opportunist trigger

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Triggers on token creature deaths")
    void triggersOnTokenDeath() {
        harness.addToBattlefield(player1, new MorbidOpportunist());
        Card tokenBear = new GrizzlyBears();
        tokenBear.setToken(true);
        harness.addToBattlefield(player1, tokenBear);
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Triggers only once when multiple creatures die simultaneously, including itself")
    void triggersOnlyOnceForSimultaneousDeaths() {
        harness.addToBattlefield(player1, new MorbidOpportunist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        seedLibrary(3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath

        // Official ruling: still triggers when Opportunist dies with other creatures.
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve Opportunist trigger

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger again later in the same turn")
    void doesNotTriggerAgainSameTurn() {
        harness.addToBattlefield(player1, new MorbidOpportunist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        seedLibrary(3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        UUID firstBearId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
        harness.castInstant(player2, 0, firstBearId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);

        UUID secondBearId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
        harness.castInstant(player2, 0, secondBearId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger on its own death alone")
    void doesNotTriggerWhenOnlySelfDies() {
        harness.addToBattlefield(player1, new MorbidOpportunist());
        seedLibrary(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID opportunistId = harness.getPermanentId(player1, "Morbid Opportunist");
        harness.castInstant(player2, 0, opportunistId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersAgainNextTurn() {
        harness.addToBattlefield(player1, new MorbidOpportunist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        seedLibrary(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID firstBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, firstBearId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);

        advanceTurn();
        advanceTurn();

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID secondBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, secondBearId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }
}
