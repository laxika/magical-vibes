package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BoonBringerValkyrie.class, PortentTracker.class})
class BoonBringerValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a counter on another creature and grants flying, first strike, and lifelink")
    void backsUpAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        Permanent valkyrie = castBoonBringerValkyrie();

        resolveEtbTargeting(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getGrantedKeywords()).containsExactlyInAnyOrder(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
        assertThat(valkyrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counter but does not grant the abilities")
    void backingUpSourceDoesNotGrantAbilities() {
        Permanent valkyrie = castBoonBringerValkyrie();

        resolveEtbTargeting(valkyrie);

        assertThat(valkyrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(valkyrie.getGrantedKeywords()).doesNotContain(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Backup's granted abilities expire at the end of the turn")
    void grantedAbilitiesExpireAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        castBoonBringerValkyrie();
        resolveEtbTargeting(bears);

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Backup can grant its counter and abilities to an opponent's creature")
    void backsUpOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PortentTracker());
        castBoonBringerValkyrie();

        resolveEtbTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).containsExactlyInAnyOrder(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Backup still grants abilities when the Valkyrie leaves before resolution")
    void backupResolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        Permanent valkyrie = castBoonBringerValkyrie();
        harness.handlePermanentChosen(player1, target.getId());

        valkyrie.setMarkedDamage(4);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(valkyrie);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).containsExactlyInAnyOrder(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Backup does not redirect to the source when its target leaves")
    void backupDoesNotResolveWithMissingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        Permanent valkyrie = castBoonBringerValkyrie();
        harness.handlePermanentChosen(player1, target.getId());

        target.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(valkyrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(valkyrie.getGrantedKeywords()).doesNotContain(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castBoonBringerValkyrie() {
        harness.castFromHand(player1, new BoonBringerValkyrie(), "{3}{W}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Boon-Bringer Valkyrie");
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
