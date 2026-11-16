package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.NyssaOfTraken;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behavior tests for the permanents a multi-slot target group judges once and hands to its later slots
 * ({@code ETBTokenTargetService.groupPermanentCandidates}): each slot offers the group's candidates less the targets
 * already chosen, and a board that changed between slots is judged again. Nyssa of Traken's reflexive ability taps
 * "up to that many target creatures", one group of as many slots as artifacts were sacrificed.
 */
@CardUsed({NyssaOfTraken.class, MindStone.class, GrizzlyBears.class})
class MultiTargetGroupCandidatesTest extends BaseCardTest {

    private Permanent nyssa;
    private List<Permanent> bears;

    private void startTargetWalk(int artifacts, int opponentBears) {
        nyssa = addCreatureReady(player1, new NyssaOfTraken());
        List<UUID> artifactIds = java.util.stream.IntStream.range(0, artifacts)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new MindStone()).getId())
                .toList();
        bears = java.util.stream.IntStream.range(0, opponentBears)
                .mapToObj(i -> addCreatureReady(player2, new GrizzlyBears()))
                .toList();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, artifacts)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nyssa)));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, artifactIds);
    }

    private List<UUID> offeredPermanents() {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        return choice.validPermanentIds();
    }

    @Test
    void eachSlotOffersTheGroupsCandidatesLessTheTargetsChosen() {
        startTargetWalk(3, 3);

        assertThat(offeredPermanents()).containsExactlyInAnyOrder(
                nyssa.getId(), bears.get(0).getId(), bears.get(1).getId(), bears.get(2).getId());
        harness.handlePermanentChosen(player1, bears.get(0).getId());

        assertThat(offeredPermanents()).containsExactlyInAnyOrder(
                nyssa.getId(), bears.get(1).getId(), bears.get(2).getId());
        harness.handlePermanentChosen(player1, bears.get(1).getId());

        assertThat(offeredPermanents()).containsExactlyInAnyOrder(nyssa.getId(), bears.get(2).getId());
        harness.handlePermanentChosen(player1, bears.get(2).getId());
        resolveAllTriggers();

        assertThat(bears).allMatch(Permanent::isTapped);
    }

    @Test
    void aCreatureThatEntersBetweenSlotsIsOfferedForTheNextSlot() {
        startTargetWalk(2, 1);
        Permanent latecomer = addCreatureReady(player2, new GrizzlyBears());

        harness.handlePermanentChosen(player1, bears.getFirst().getId());

        assertThat(offeredPermanents()).contains(latecomer.getId());
    }

    @Test
    void aCreatureThatLeavesBetweenSlotsIsNotOfferedForTheNextSlot() {
        startTargetWalk(2, 2);
        gd.playerBattlefields.get(player2.getId()).remove(bears.get(1));

        harness.handlePermanentChosen(player1, bears.getFirst().getId());

        assertThat(offeredPermanents()).doesNotContain(bears.get(1).getId());
    }
}
