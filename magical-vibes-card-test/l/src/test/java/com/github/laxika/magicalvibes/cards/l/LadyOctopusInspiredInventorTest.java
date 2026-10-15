package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.c.ChromaticLantern;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LadyOctopusInspiredInventor.class, GrizzlyBears.class, IchorWellspring.class,
        ChromaticLantern.class})
class LadyOctopusInspiredInventorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts ingenuity counters on itself for the first two cards drawn each turn")
    void triggersOnFirstAndSecondCardDrawn() {
        Permanent lady = harness.addToBattlefieldAndReturn(player1, new LadyOctopusInspiredInventor());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawAndResolveTrigger(player1);
        assertThat(lady.getCounterCount(CounterType.INGENUITY)).isEqualTo(1);

        drawAndResolveTrigger(player1);
        assertThat(lady.getCounterCount(CounterType.INGENUITY)).isEqualTo(2);

        drawAndResolveTrigger(player1);
        assertThat(lady.getCounterCount(CounterType.INGENUITY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Offers a nonland artifact from hand whose mana value is at most its ingenuity counters")
    void offersArtifactWithinManaValueLimit() {
        Permanent lady = harness.addToBattlefieldAndReturn(player1, new LadyOctopusInspiredInventor());
        lady.setSummoningSick(false);
        lady.setCounterCount(CounterType.INGENUITY, 2);
        IchorWellspring wellspring = new IchorWellspring();
        harness.setHand(player1, List.of(wellspring, new ChromaticLantern(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).contains("Ichor Wellspring");
        assertThat(choice.description()).doesNotContain("Chromatic Lantern", "Grizzly Bears");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(wellspring.getId());
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(wellspring.getId()));
    }

    @Test
    void decliningDoesNotRevealArtifactInHand() {
        Permanent lady = harness.addToBattlefieldAndReturn(player1, new LadyOctopusInspiredInventor());
        lady.setSummoningSick(false);
        lady.setCounterCount(CounterType.INGENUITY, 2);
        IchorWellspring wellspring = new IchorWellspring();
        harness.setHand(player1, List.of(wellspring));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wellspring);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(message -> message.contains("Ichor Wellspring"));
    }

    @Test
    void castsOnlyOneArtifactPerActivation() {
        Permanent lady = harness.addToBattlefieldAndReturn(player1, new LadyOctopusInspiredInventor());
        lady.setSummoningSick(false);
        lady.setCounterCount(CounterType.INGENUITY, 3);
        IchorWellspring wellspring = new IchorWellspring();
        ChromaticLantern lantern = new ChromaticLantern();
        harness.setHand(player1, List.of(wellspring, lantern));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(wellspring.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lantern);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(lady.isTapped()).isTrue();
        assertThat(lady.getCounterCount(CounterType.INGENUITY)).isEqualTo(3);
    }

    @Test
    void usesCounterCountAtResolution() {
        Permanent lady = harness.addToBattlefieldAndReturn(player1, new LadyOctopusInspiredInventor());
        lady.setSummoningSick(false);
        lady.setCounterCount(CounterType.INGENUITY, 2);
        harness.setHand(player1, List.of(new IchorWellspring()));

        harness.activateAbility(player1, 0, 0, null, null);
        lady.setCounterCount(CounterType.INGENUITY, 1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerForOpponentDraws() {
        Permanent lady = harness.addToBattlefieldAndReturn(player1, new LadyOctopusInspiredInventor());
        harness.setLibrary(player2, List.of(new LadyOctopusInspiredInventor()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(lady.getCounterCount(CounterType.INGENUITY)).isZero();
    }

    @Test
    void countsDrawsBeforeEnteringBattlefield() {
        harness.setLibrary(player1, List.of(new LadyOctopusInspiredInventor(),
                new LadyOctopusInspiredInventor(), new LadyOctopusInspiredInventor()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        Permanent lady = harness.addToBattlefieldAndReturn(player1, new LadyOctopusInspiredInventor());

        drawAndResolveTrigger(player1);
        assertThat(lady.getCounterCount(CounterType.INGENUITY)).isEqualTo(1);

        drawAndResolveTrigger(player1);
        assertThat(lady.getCounterCount(CounterType.INGENUITY)).isEqualTo(1);
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
