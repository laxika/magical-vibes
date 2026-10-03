package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LiquimetalTorque;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundShikari.class, BronzeSable.class, GrizzlyBears.class, Assassinate.class, LiquimetalTorque.class})
class ArcboundShikariTest extends BaseCardTest {

    @Test
    void entersWithTwoCountersAndCountersOtherArtifactCreatures() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.castFromHand(player1, new ArcboundShikari(), "{1}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent shikari = findPermanent(player1, "Arcbound Shikari");
        assertThat(shikari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonartifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentArtifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreature() {
        Permanent shikari = addCreatureReady(player1, new ArcboundShikari());
        shikari.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        shikari.tap();
        Permanent artifactCreature = addCreatureReady(player1, new BronzeSable());
        Permanent nonartifactCreature = addCreatureReady(player1, new GrizzlyBears());

        destroyShikari(shikari);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(artifactCreature.getId())
                .doesNotContain(nonartifactCreature.getId());

        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void destroyShikari(Permanent shikari) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player2, 0, shikari.getId());
        harness.passBothPriorities();
    }

    @Test
    void entryCountersArePresentBeforeTriggerAndNoncreatureArtifactsAreExcluded() {
        Permanent otherShikari = addCreatureReady(player1, new ArcboundShikari());
        otherShikari.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LiquimetalTorque());

        harness.castFromHand(player1, new ArcboundShikari(), "{1}{R}{W}");
        harness.passBothPriorities();

        Permanent enteringShikari = findPermanents(player1, "Arcbound Shikari").getLast();
        assertThat(enteringShikari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherShikari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(enteringShikari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherShikari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularCanBeDeclinedAfterChoosingATarget() {
        Permanent shikari = addCreatureReady(player1, new ArcboundShikari());
        shikari.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        shikari.tap();
        Permanent target = addCreatureReady(player1, new ArcboundShikari());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        destroyShikari(shikari);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        String description = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .description();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Arcbound Shikari");
        assertThat(description).contains("+1/+1").doesNotContain("-1/-1");
    }

    @Test
    void modularCanGiveAllCountersAtDeathToAnOpponentsArtifactCreature() {
        Permanent shikari = addCreatureReady(player1, new ArcboundShikari());
        shikari.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        shikari.tap();
        Permanent target = addCreatureReady(player2, new ArcboundShikari());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        destroyShikari(shikari);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void firstStrikeKillsBlockerBeforeItCanDealDamage() {
        Permanent shikari = addCreatureReady(player1, new ArcboundShikari());
        shikari.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shikari);
        assertThat(shikari.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
