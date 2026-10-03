package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.m.MeliraSylvokOutcast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightedBlackthorn.class, SerraAngel.class, MeliraSylvokOutcast.class})
class BlightedBlackthornTest extends BaseCardTest {

    @Test
    void acceptingEnterTriggerBlightsChosenCreatureDrawsAndLosesLife() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        BlightedBlackthorn blackthorn = new BlightedBlackthorn();
        Card drawnCard = new SerraAngel();
        gd.playerDecks.get(player1.getId()).addFirst(drawnCard);
        harness.setHand(player1, List.of(blackthorn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(otherCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void decliningEnterTriggerDoesNothing() {
        BlightedBlackthorn blackthorn = new BlightedBlackthorn();
        harness.setHand(player1, List.of(blackthorn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == blackthorn
                        && permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) == 0);
    }

    @Test
    void attackTriggerCanBeAccepted() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        BlightedBlackthorn blackthorn = new BlightedBlackthorn();
        Permanent blackthornPermanent = harness.addToBattlefieldAndReturn(player1, blackthorn);
        blackthornPermanent.setSummoningSick(false);
        Card drawnCard = new SerraAngel();
        gd.playerDecks.get(player1.getId()).addFirst(drawnCard);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(blackthornPermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void drawAndLifeLossResolveImmediatelyAfterBlighting() {
        Permanent blackthorn = harness.enterBattlefieldAndReturn(player1, new BlightedBlackthorn());
        Card drawnCard = new BlightedBlackthorn();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blackthorn.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lethalBlightStillDrawsAndLosesLifeDuringTheOriginalResolution() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new BlightedBlackthorn());
        otherCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 6);
        harness.enterBattlefieldAndReturn(player1, new BlightedBlackthorn());
        Card drawnCard = new BlightedBlackthorn();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherCreature.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        harness.assertLife(player1, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBlightAnOpponentsCreatureWhenNoControlledCreatureRemains() {
        harness.enterBattlefieldAndReturn(player1, new BlightedBlackthorn());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BlightedBlackthorn());
        gd.playerBattlefields.get(player1.getId()).clear();
        Card drawnCard = new BlightedBlackthorn();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(opposingCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(drawnCard);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void decliningAttackTriggerDoesNotBlightDrawOrLoseLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent blackthorn = harness.addToBattlefieldAndReturn(player1, new BlightedBlackthorn());
        blackthorn.setSummoningSick(false);
        Card drawnCard = new BlightedBlackthorn();
        harness.setLibrary(player1, List.of(drawnCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blackthorn.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(drawnCard);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReceiveTheRewardWhenTheOnlyCreatureCannotReceiveBlightCounters() {
        Permanent melira = harness.addToBattlefieldAndReturn(player1, new MeliraSylvokOutcast());
        Permanent blackthorn = harness.enterBattlefieldAndReturn(player1, new BlightedBlackthorn());
        gd.playerBattlefields.get(player1.getId()).remove(blackthorn);
        harness.setGraveyard(player1, List.of(blackthorn.getCard()));
        Card drawnCard = new BlightedBlackthorn();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(melira.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(drawnCard);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
