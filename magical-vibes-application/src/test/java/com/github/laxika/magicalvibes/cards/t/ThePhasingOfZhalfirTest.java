package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePhasingOfZhalfir.class, GrizzlyBears.class, Island.class, MindStone.class})
class ThePhasingOfZhalfirTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I phases out another nonland permanent until the Saga leaves")
    void chapterIPhasesOutPermanentUntilSagaLeaves() {
        Permanent target = addReady(player2, new MindStone());
        addSaga(player1, 0);

        triggerChapter();
        chooseTarget(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target);

        advanceToUntap(player2);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("The Phasing of Zhalfir"));
        advanceToUntap(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Chapter targeting only offers another nonland permanent as a target")
    void chaptersOnlyOfferLegalTargets() {
        Permanent land = new Permanent(new Island());
        gd.playerBattlefields.get(player2.getId()).add(land);
        Permanent target = addReady(player2, new MindStone());
        Permanent saga = addSaga(player1, 0);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(land.getId(), saga.getId());

        chooseTarget(target);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Chapter III destroys creatures and gives each controller one Phyrexian per creature")
    void chapterIIICreatesTokensForDestroyedCreatures() {
        addSaga(player1, 2);
        addReady(player1, new GrizzlyBears());
        addReady(player1, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());

        triggerChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Phyrexian")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN);
        });
        assertThat(findPermanents(player2, "Phyrexian")).hasSize(1);
    }

    private Permanent addSaga(Player player, int loreCounters) {
        Permanent saga = new Permanent(new ThePhasingOfZhalfir());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player.getId()).add(saga);
        return saga;
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void chooseTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToUntap(Player player) {
        harness.performUntapStep(player);
    }
}
