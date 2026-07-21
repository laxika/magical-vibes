package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeddingAnnouncementTest extends BaseCardTest {

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("End step without attacking: invitation counter + Human token")
    void endStepCreatesTokenWhenNotAttacked() {
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        int creaturesBefore = countCreatures(player1);

        resolveEndStepTrigger();

        assertThat(announcement.getCounterCount(CounterType.INVITATION)).isEqualTo(1);
        assertThat(countCreatures(player1)).isEqualTo(creaturesBefore + 1);
        assertThat(announcement.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("End step after attacking with 2+: invitation counter + draw")
    void endStepDrawsWhenAttackedWithTwo() {
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int creaturesBefore = countCreatures(player1);

        resolveEndStepTrigger();

        assertThat(announcement.getCounterCount(CounterType.INVITATION)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(countCreatures(player1)).isEqualTo(creaturesBefore);
        assertThat(announcement.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforms on third invitation counter and keeps counters")
    void transformsAtThreeCounters() {
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        announcement.setCounterCount(CounterType.INVITATION, 2);

        resolveEndStepTrigger();

        assertThat(announcement.isTransformed()).isTrue();
        assertThat(announcement.getCounterCount(CounterType.INVITATION)).isEqualTo(3);
        assertThat(announcement.getCard().getName()).isEqualTo("Wedding Festivity");
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(announcement.getCounterCount(CounterType.INVITATION)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Wedding Festivity gives creatures you control +1/+1")
    void festivityBoostsOwnCreatures() {
        WeddingAnnouncement card = new WeddingAnnouncement();
        Permanent festivity = new Permanent(card);
        festivity.setCard(card.getBackFaceCard());
        festivity.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(festivity);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    private int countCreatures(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count();
    }
}
