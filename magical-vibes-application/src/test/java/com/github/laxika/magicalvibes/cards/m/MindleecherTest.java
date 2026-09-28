package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindleecher.class, Forest.class, GrizzlyBears.class})
class MindleecherTest extends BaseCardTest {

    @Test
    void mutationExilesTopCardOfEachOpponentsLibraryFaceDownAndAllowsPlayingThem() {
        Permanent mindleecher = addCreatureReady(player1, new Mindleecher());
        Card opponentLand = new Forest();
        Card opponentCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentCreature, opponentLand));

        triggerMutation(mindleecher);

        assertThat(gd.getCardsExiledByPermanent(mindleecher.getId()))
                .containsExactly(opponentCreature);
        assertThat(gd.exiledCards).filteredOn(entry -> mindleecher.getId().equals(entry.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentCreature);
    }

    @Test
    void playPermissionRemainsAfterMindleecherLeaves() {
        Permanent mindleecher = addCreatureReady(player1, new Mindleecher());
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled));

        triggerMutation(mindleecher);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mindleecher);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiled);
    }

    private void triggerMutation(Permanent mindleecher) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, mindleecher, List.of(mindleecher.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
