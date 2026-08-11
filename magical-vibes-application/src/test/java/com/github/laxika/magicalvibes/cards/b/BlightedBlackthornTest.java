package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
