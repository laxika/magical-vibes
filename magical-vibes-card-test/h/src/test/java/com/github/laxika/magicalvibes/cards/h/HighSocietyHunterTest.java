package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighSocietyHunter.class, GrizzlyBears.class, Shock.class, DayOfJudgment.class})
class HighSocietyHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers sacrificing another creature")
    void attackingOffersSacrifice() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), secondBears.getId());
        assertThat(choice.validIds()).doesNotContain(hunter.getId());
    }

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on High-Society Hunter")
    void sacrificingAnotherCreaturePutsCounter() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the battlefield unchanged")
    void decliningSacrificeDoesNothing() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("With no other creature, attacking does not create a sacrifice choice")
    void noOtherCreatureDoesNothing() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws a card when another nontoken creature dies")
    void drawsOnNontokenCreatureDeath() {
        harness.addToBattlefield(player1, new HighSocietyHunter());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when a token creature dies")
    void tokenDeathDoesNotDraw() {
        harness.addToBattlefield(player1, new HighSocietyHunter());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        Card tokenBear = new GrizzlyBears();
        tokenBear.setToken(true);
        harness.addToBattlefield(player2, tokenBear);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Each Hunter draws for the other Hunter dying simultaneously")
    void simultaneousDeathsTriggerBothHunters() {
        harness.addToBattlefield(player1, new HighSocietyHunter());
        harness.addToBattlefield(player1, new HighSocietyHunter());
        harness.setLibrary(player1, List.of(new HighSocietyHunter(), new HighSocietyHunter()));
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Hunter does not draw for its own death")
    void ownDeathDoesNotDraw() {
        harness.addToBattlefield(player1, new HighSocietyHunter());
        harness.setLibrary(player1, List.of(new HighSocietyHunter()));
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing a nontoken sacrifice gives a counter and draws a card")
    void chosenSacrificeAlsoDraws() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());
        Permanent sacrifice = addCreatureReady(player1, new HighSocietyHunter());
        Permanent survivor = addCreatureReady(player1, new HighSocietyHunter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HighSocietyHunter(), new HighSocietyHunter()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(hunter, survivor);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A token can be sacrificed for a counter without drawing")
    void tokenSacrificeGivesCounterWithoutDrawing() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());
        Card token = new HighSocietyHunter();
        token.setToken(true);
        Permanent sacrifice = addCreatureReady(player1, token);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HighSocietyHunter()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hunter).doesNotContain(sacrifice);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
