package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HamletCaptain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeddingAnnouncement.class, HamletCaptain.class, WeaverOfHarmony.class})
class WeddingAnnouncementTest extends BaseCardTest {

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires
        resolveAllTriggers();
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
    void createsOneOneWhiteHumanToken() {
        harness.addToBattlefield(player1, new WeddingAnnouncement());

        resolveEndStepTrigger();

        Permanent token = findPermanent(player1, "Human");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("End step after attacking with 2+: invitation counter + draw")
    void endStepDrawsWhenAttackedWithTwo() {
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        Permanent firstAttacker = addCreatureReady(player1, new HamletCaptain());
        Permanent secondAttacker = addCreatureReady(player1, new HamletCaptain());
        gd.creaturesAttackedWithThisTurn.put(player1.getId(),
                java.util.Set.of(firstAttacker.getId(), secondAttacker.getId()));
        harness.setLibrary(player1, List.of(new WeddingAnnouncement()));
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
        Permanent festivity = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        festivity.setCounterCount(CounterType.INVITATION, 2);
        resolveEndStepTrigger();

        Permanent captain = harness.addToBattlefieldAndReturn(player1, new HamletCaptain());
        Permanent opponentCaptain = harness.addToBattlefieldAndReturn(player2, new HamletCaptain());

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCaptain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCaptain)).isEqualTo(2);
    }

    @Test
    void oneAttackerStillCreatesToken() {
        Permanent attacker = addCreatureReady(player1, new HamletCaptain());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveEndStepTrigger();

        assertThat(announcement.getCounterCount(CounterType.INVITATION)).isEqualTo(1);
        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.creaturesAttackedWithThisTurn.get(player1.getId())).containsExactly(attacker.getId());
    }

    @Test
    void drawsForAttackersThatLeftBeforeAnnouncementEntered() {
        addCreatureReady(player1, new HamletCaptain());
        addCreatureReady(player1, new HamletCaptain());
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new WeddingAnnouncement());
        harness.setLibrary(player1, List.of(new WeddingAnnouncement()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(countCreatures(player1)).isZero();
    }

    @Test
    void createsTokenEvenWhenSourceLeavesBeforeResolution() {
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(announcement);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
    }

    @Test
    void transformedFaceDoesNotProduceFurtherEndStepTriggers() {
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        announcement.setCounterCount(CounterType.INVITATION, 2);
        resolveEndStepTrigger();

        resolveEndStepTrigger();

        assertThat(announcement.isTransformed()).isTrue();
        assertThat(announcement.getCounterCount(CounterType.INVITATION)).isEqualTo(3);
        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Human");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void drawsAndTransformsWithAtLeastThreeCounters() {
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        announcement.setCounterCount(CounterType.INVITATION, 4);
        Permanent first = addCreatureReady(player1, new HamletCaptain());
        Permanent second = addCreatureReady(player1, new HamletCaptain());
        gd.creaturesAttackedWithThisTurn.put(player1.getId(), java.util.Set.of(first.getId(), second.getId()));
        harness.setLibrary(player1, List.of(new WeddingAnnouncement()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(announcement.isTransformed()).isFalse();
        resolveEndStepTrigger();

        assertThat(announcement.isTransformed()).isTrue();
        assertThat(announcement.getCounterCount(CounterType.INVITATION)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(countPermanents(player1, "Human")).isZero();
    }

    @Test
    void copiedTriggerCannotTransformFestivityBack() {
        addCreatureReady(player1, new WeaverOfHarmony());
        Permanent announcement = harness.addToBattlefieldAndReturn(player1, new WeddingAnnouncement());
        announcement.setCounterCount(CounterType.INVITATION, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        var triggerId = gd.stack.getLast().getTargetableId();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, triggerId);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(announcement.getCounterCount(CounterType.INVITATION)).isEqualTo(4);
        assertThat(countPermanents(player1, "Human")).isEqualTo(2);
        assertThat(announcement.isTransformed()).isTrue();
        assertThat(announcement.getCard().getName()).isEqualTo("Wedding Festivity");
    }

    private int countCreatures(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count();
    }
}
