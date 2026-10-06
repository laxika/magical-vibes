package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PortentTracker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerpentBladeAssailant.class, PortentTracker.class})
class SerpentBladeAssailantTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and grants deathtouch")
    void backsUpAnotherCreature() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        castAssailant();

        resolveEtbTargeting(tracker);

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tracker.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counter but does not grant deathtouch")
    void backingUpSourceDoesNotGrantDeathtouch() {
        castAssailant();
        Permanent assailant = findPermanent(player1, "Serpent-Blade Assailant");

        resolveEtbTargeting(assailant);

        assertThat(assailant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(assailant.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Backup's granted deathtouch expires at the end of the turn")
    void grantedDeathtouchExpiresAtEndOfTurn() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        castAssailant();
        resolveEtbTargeting(tracker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tracker.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Backup can target an opponent's creature")
    void backsUpOpponentsCreature() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player2, new PortentTracker());
        castAssailant();

        resolveEtbTargeting(tracker);

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tracker.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(findPermanent(player1, "Serpent-Blade Assailant").hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Backup resolves even if the source leaves the battlefield")
    void backupResolvesAfterSourceLeaves() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        castAssailant();
        harness.handlePermanentChosen(player1, tracker.getId());
        Permanent assailant = findPermanent(player1, "Serpent-Blade Assailant");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, assailant));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serpent-Blade Assailant");
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tracker.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Backup does not affect any other creature if its target leaves")
    void backupDoesNotRetargetAfterTargetLeaves() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        castAssailant();
        harness.handlePermanentChosen(player1, tracker.getId());
        Permanent assailant = findPermanent(player1, "Serpent-Blade Assailant");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, tracker));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Portent Tracker");
        assertThat(assailant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(assailant.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    private void castAssailant() {
        harness.castFromHand(player1, new SerpentBladeAssailant(), "{2}{G}");
        harness.passBothPriorities();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
