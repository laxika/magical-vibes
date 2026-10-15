package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Caprichrome.class, DarksteelRelic.class, GrizzlyBears.class})
class CaprichromeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Caprichrome a +1/+1 counter")
    void sacrificingArtifactAddsCounter() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCaprichrome();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(relic.getId()));

        assertThat(findPermanent(player1, "Caprichrome")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Darksteel Relic")).isZero();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing no artifacts leaves Caprichrome without counters")
    void choosingNoArtifactsAddsNoCounters() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCaprichrome();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Caprichrome")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @CardUsed({Caprichrome.class})
    void mayDeclineToDevourAvailableArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Caprichrome());

        castCaprichrome();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(findPermanents(player1, "Caprichrome")).hasSize(2);
        assertThat(findPermanents(player1, "Caprichrome"))
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        harness.assertNotInGraveyard(player1, "Caprichrome");
    }

    @Test
    @CardUsed({Caprichrome.class})
    void devoursMultipleControlledArtifactCreaturesButNotOpponentOrItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Caprichrome());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Caprichrome());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Caprichrome());

        castCaprichrome();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                (PendingInteraction.MultiPermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(findPermanents(player1, "Caprichrome")).hasSize(1);
        assertThat(findPermanent(player1, "Caprichrome")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(first.getCard(), second.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Caprichrome.class})
    void flashAllowsCastingDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        castCaprichrome();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Caprichrome");
        assertThat(findPermanent(player1, "Caprichrome")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({Caprichrome.class})
    void vigilanceKeepsCaprichromeUntappedWhenAttacking() {
        Permanent goat = addCreatureReady(player1, new Caprichrome());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(goat.isAttacking()).isTrue();
        assertThat(goat.isTapped()).isFalse();
    }

    private void castCaprichrome() {
        harness.castFromHand(player1, new Caprichrome(), "{3}{W}");
    }
}
