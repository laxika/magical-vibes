package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.f.FamishedForagers;
import com.github.laxika.magicalvibes.cards.w.WrennAndSeven;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlayWithFire.class, CandlegroveWitch.class, FamishedForagers.class, WrennAndSeven.class})
class PlayWithFireTest extends BaseCardTest {

    @Test
    void damagesPlayerThenScries() {
        Card topCard = new CandlegroveWitch();
        Card nextCard = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damagesCreatureWithoutScrying() {
        var target = harness.addToBattlefieldAndReturn(player2, new FamishedForagers());
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canPutTheScriedCardOnTheBottom() {
        Card topCard = new CandlegroveWitch();
        Card nextCard = new CandlegroveWitch();
        Card opponentTopCard = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Play with Fire");
    }

    @Test
    void targetingYourselfStillScriesYourLibrary() {
        Card topCard = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fullyPreventedDamageDoesNotScryEvenAfterEarlierPlayerDamage() {
        Card topCard = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new PlayWithFire(), new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gd.playerDamagePreventionShields.put(player2.getId(), 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void partiallyPreventedDamageStillScries() {
        Card topCard = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        gd.playerDamagePreventionShields.put(player2.getId(), 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damagesPlaneswalkerWithoutScrying() {
        Card topCard = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(topCard));
        var target = harness.addToBattlefieldAndReturn(player2, new WrennAndSeven());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lethalCreatureDamageDoesNotScry() {
        Card topCard = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(topCard));
        var target = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
        harness.assertInGraveyard(player2, "Candlegrove Witch");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedTargetMakesTheSpellDoNothing() {
        Card topCard = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(topCard));
        var target = harness.addToBattlefieldAndReturn(player2, new FamishedForagers());
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Play with Fire");
    }

    @Test
    void emptyLibraryDoesNotInterruptResolution() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new PlayWithFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Play with Fire");
    }
}
