package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WorkshopElders.class, ArcboundWorker.class, CrazedGoblin.class, DarksteelRelic.class})
class WorkshopEldersTest extends BaseCardTest {

    @Test
    @DisplayName("Gives flying to artifact creatures you control")
    void givesFlyingToArtifactCreaturesYouControl() {
        harness.addToBattlefieldAndReturn(player1, new WorkshopElders());
        Permanent ownArtifactCreature = harness.addToBattlefieldAndReturn(player1, new ArcboundWorker());
        Permanent ownNonartifactCreature = harness.addToBattlefieldAndReturn(player1, new CrazedGoblin());
        Permanent ownNoncreatureArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new ArcboundWorker());

        assertThat(gqs.hasKeyword(gd, ownArtifactCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonartifactCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownNoncreatureArtifact, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifactCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Animates a target noncreature artifact and puts four counters on it")
    void animatesTargetAndAddsCounters() {
        harness.addToBattlefieldAndReturn(player1, new WorkshopElders());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        resolveBeginningOfCombatTrigger();
        harness.handlePermanentChosen(player1, relic.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.isArtifact(gd, relic)).isTrue();
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(relic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the beginning-of-combat ability does nothing")
    void decliningAbilityDoesNothing() {
        harness.addToBattlefieldAndReturn(player1, new WorkshopElders());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        resolveBeginningOfCombatTrigger();
        harness.handlePermanentChosen(player1, relic.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.isCreature(gd, relic)).isFalse();
        assertThat(relic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The beginning-of-combat ability only targets noncreature artifacts you control")
    void targetMustBeNoncreatureArtifactYouControl() {
        harness.addToBattlefieldAndReturn(player1, new WorkshopElders());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArcboundWorker());
        harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent opponentRelic = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());

        resolveBeginningOfCombatTrigger();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentRelic.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void resolveBeginningOfCombatTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
