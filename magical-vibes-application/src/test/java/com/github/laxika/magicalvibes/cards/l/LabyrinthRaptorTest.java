package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CavernWhisperer;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoneyMammoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LabyrinthRaptor.class, GiantSpider.class, GrizzlyBears.class,
        CavernWhisperer.class, HoneyMammoth.class})
class LabyrinthRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("A menace creature becoming blocked makes the defending player choose a blocker to sacrifice")
    void defendingPlayerSacrificesAChosenBlocker() {
        addCreatureReady(player1, new LabyrinthRaptor());
        Permanent firstBlocker = addCreatureReady(player2, new GiantSpider());
        Permanent secondBlocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(firstBlocker.getId(), secondBlocker.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(firstBlocker.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstBlocker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondBlocker.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstBlocker.getId()));
    }

    @Test
    @DisplayName("The activation boosts only creatures you control with menace")
    void activationBoostsMenaceCreaturesOnly() {
        Permanent raptor = addCreatureReady(player1, new LabyrinthRaptor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another menace attacker triggers a sacrifice restricted to its own blockers")
    void anotherMenaceAttackerTriggersSacrifice() {
        addCreatureReady(player1, new LabyrinthRaptor());
        addCreatureReady(player1, new CavernWhisperer());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent firstBlocker = addCreatureReady(player2, new HoneyMammoth());
        Permanent secondBlocker = addCreatureReady(player2, new HoneyMammoth());
        Permanent unrelatedBlocker = addCreatureReady(player2, new HoneyMammoth());

        declareAttackersAndPrepareBlockers(List.of(1, 2));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1),
                new BlockerAssignment(2, 2)));
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(firstBlocker.getId(), secondBlocker.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(secondBlocker.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondBlocker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(firstBlocker, unrelatedBlocker);
    }

    @Test
    @DisplayName("A blocked creature without menace does not trigger a sacrifice")
    void nonMenaceAttackerDoesNotTrigger() {
        addCreatureReady(player1, new LabyrinthRaptor());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HoneyMammoth());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Repeated activations boost allied menace creatures but not opposing ones")
    void repeatedActivationsBoostAlliedMenaceCreatures() {
        Permanent raptor = addCreatureReady(player1, new LabyrinthRaptor());
        Permanent ally = addCreatureReady(player1, new CavernWhisperer());
        Permanent opponent = addCreatureReady(player2, new CavernWhisperer());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(4);

        Permanent lateArrival = addCreatureReady(player1, new CavernWhisperer());
        assertThat(gqs.getEffectivePower(gd, lateArrival)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Raptors each require a sacrifice when one menace creature becomes blocked")
    void multipleRaptorsEachTrigger() {
        addCreatureReady(player1, new LabyrinthRaptor());
        addCreatureReady(player1, new LabyrinthRaptor());
        Permanent firstBlocker = addCreatureReady(player2, new HoneyMammoth());
        Permanent secondBlocker = addCreatureReady(player2, new HoneyMammoth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(firstBlocker.getId(), secondBlocker.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(firstBlocker.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstBlocker.getCard(), secondBlocker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
