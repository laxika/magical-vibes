package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.o.Oxidize;
import com.github.laxika.magicalvibes.cards.s.Skullclamp;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcboundReclaimer.class, CrazedGoblin.class, DarksteelGargoyle.class, Oxidize.class,
        Skullclamp.class})
class ArcboundReclaimerTest extends BaseCardTest {

    @Test
    void entersWithTwoPlusOnePlusOneCounters() {
        harness.castFromHand(player1, new ArcboundReclaimer(), "{4}");
        harness.passBothPriorities();

        Permanent reclaimer = findPermanent(player1, "Arcbound Reclaimer");
        assertThat(reclaimer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void removesACounterAndPutsTargetArtifactFromGraveyardOnTopOfLibrary() {
        Permanent reclaimer = addCreatureReady(player1, new ArcboundReclaimer());
        reclaimer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card artifact = new DarksteelGargoyle();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(new CrazedGoblin()));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(reclaimer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(artifact.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    void cannotTargetNonArtifactCardInGraveyard() {
        Permanent reclaimer = addCreatureReady(player1, new ArcboundReclaimer());
        reclaimer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card nonArtifact = new CrazedGoblin();
        harness.setGraveyard(player1, List.of(nonArtifact));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutPlusOnePlusOneCounter() {
        addCreatureReady(player1, new ArcboundReclaimer());
        Card artifact = new DarksteelGargoyle();
        harness.setGraveyard(player1, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityCannotTargetArtifactInOpponentsGraveyard() {
        Permanent reclaimer = addCreatureReady(player1, new ArcboundReclaimer());
        reclaimer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card artifact = new DarksteelGargoyle();
        harness.setGraveyard(player2, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent reclaimer = addCreatureReady(player1, new ArcboundReclaimer());
        reclaimer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());
        Permanent skullclamp = harness.addToBattlefieldAndReturn(player1, new Skullclamp());

        destroyReclaimer(reclaimer);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId())
                .doesNotContain(goblin.getId(), skullclamp.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularCanPutItsCountersOnOpponentsArtifactCreature() {
        Permanent reclaimer = addCreatureReady(player1, new ArcboundReclaimer());
        reclaimer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        destroyReclaimer(reclaimer);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularMayBeDeclined() {
        Permanent reclaimer = addCreatureReady(player1, new ArcboundReclaimer());
        reclaimer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyReclaimer(reclaimer);

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void destroyReclaimer(Permanent reclaimer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Oxidize()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, reclaimer.getId());
    }
}
