package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MorbidOpportunist.class, Forest.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
class MorbidOpportunistTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private void seedLibrary(int count) {
        harness.setLibrary(player1, IntStream.range(0, count).mapToObj(i -> new Forest()).toList());
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
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Opportunist trigger

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Forest");
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
        harness.castAndResolveInstant(player2, 0, bearsId);
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

        harness.castSorcery(player2, 0);
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

        UUID firstBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, firstBearId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);

        UUID secondBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, secondBearId);

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
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Morbid Opportunist");
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
        harness.castAndResolveInstant(player2, 0, firstBearId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);

        advanceTurn();
        advanceTurn();

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID secondBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, secondBearId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }

    @Test
    @DisplayName("Draws for an opponent's creature dying")
    void drawsWhenOpponentsCreatureDies() {
        harness.addToBattlefield(player1, new MorbidOpportunist());
        harness.addToBattlefield(player2, new GrizzlyBears());
        seedLibrary(1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Each Opportunist has its own once-per-turn limit")
    void eachCopyTriggersIndependently() {
        harness.addToBattlefield(player1, new MorbidOpportunist());
        harness.addToBattlefield(player1, new MorbidOpportunist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        seedLibrary(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("A pending draw trigger already consumes the turn's trigger allowance")
    void doesNotTriggerAgainBeforeFirstTriggerResolves() {
        harness.addToBattlefield(player1, new MorbidOpportunist());
        UUID firstBearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID secondBearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        seedLibrary(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, firstBearId);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, secondBearId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
