package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonatelloMutantMechanic.class, DarksteelRelic.class, Memnite.class})
class DonatelloMutantMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("Puts three +1/+1 counters on and animates a noncreature artifact")
    void animatesNoncreatureArtifact() {
        Permanent donatello = addCreatureReady(player1, new DonatelloMutantMechanic());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        activate(donatello, relic);

        assertThat(relic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, relic)).isTrue();
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, relic, CardSubtype.ROBOT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not animate an artifact that is already a creature")
    void doesNotAnimateArtifactCreature() {
        Permanent donatello = addCreatureReady(player1, new DonatelloMutantMechanic());
        Permanent memnite = addCreatureReady(player1, new Memnite());

        activate(donatello, memnite);

        assertThat(memnite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, memnite)).isTrue();
        assertThat(gqs.isCreature(gd, memnite)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, memnite, CardSubtype.ROBOT)).isFalse();
        assertThat(gqs.getEffectivePower(gd, memnite)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, memnite)).isEqualTo(4);
    }

    @Test
    @DisplayName("Moves all counters from a controlled artifact put into a graveyard")
    void movesCountersFromArtifactPutIntoGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new DonatelloMutantMechanic());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = addCreatureReady(player1, new Memnite());
        relic.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        relic.setCounterCount(CounterType.CHARGE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, relic));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    private void activate(Permanent donatello, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int donatelloIndex = gd.playerBattlefields.get(player1.getId()).indexOf(donatello);
        harness.activateAbility(player1, donatelloIndex, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
