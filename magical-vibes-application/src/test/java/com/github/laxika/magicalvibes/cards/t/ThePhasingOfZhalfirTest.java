package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SalvagedManaworker;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePhasingOfZhalfir.class, GrizzlyBears.class, Island.class, MindStone.class,
        SalvagedManaworker.class})
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

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Read ahead can start at either phase-out chapter without triggering skipped chapters")
    void readAheadStartsAtPhaseOutChapter(int chapter) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SalvagedManaworker());
        harness.castFromHand(player1, new ThePhasingOfZhalfir(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, Integer.toString(chapter));
        chooseTarget(target);

        assertThat(findPermanent(player1, "The Phasing of Zhalfir").getCounterCount(CounterType.LORE))
                .isEqualTo(chapter);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(target);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Read ahead can skip directly to the creature wipe")
    void readAheadStartsAtChapterThree() {
        harness.addToBattlefield(player1, new SalvagedManaworker());
        harness.addToBattlefield(player2, new SalvagedManaworker());
        harness.castFromHand(player1, new ThePhasingOfZhalfir(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "3");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Salvaged Manaworker");
        harness.assertInGraveyard(player2, "Salvaged Manaworker");
        assertThat(findPermanents(player1, "Phyrexian")).hasSize(1);
        assertThat(findPermanents(player2, "Phyrexian")).hasSize(1);
        harness.assertInGraveyard(player1, "The Phasing of Zhalfir");
    }

    @Test
    @DisplayName("Chapter II can protect your creature from chapter III until your next untap")
    void chapterIIProtectsCreatureFromChapterIII() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new SalvagedManaworker());
        harness.addToBattlefield(player2, new SalvagedManaworker());
        addSaga(player1, 1);

        triggerChapter();
        chooseTarget(protectedCreature);
        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(protectedCreature);
        assertThat(findPermanents(player1, "Phyrexian")).isEmpty();
        assertThat(findPermanents(player2, "Phyrexian")).hasSize(1);
        harness.assertInGraveyard(player1, "The Phasing of Zhalfir");
        harness.assertInGraveyard(player2, "Salvaged Manaworker");

        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
    }

    @Test
    @DisplayName("Destroyed creature tokens are replaced too, while noncreature permanents survive")
    void chapterIIIReplacesCreatureTokensAndPreservesNoncreatures() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.addToBattlefield(player1, new SalvagedManaworker());
        addSaga(player1, 2);
        triggerChapter();
        harness.passBothPriorities();
        Permanent originalToken = findPermanent(player1, "Phyrexian");

        addSaga(player1, 2);
        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(originalToken);
        assertThat(findPermanents(player1, "Phyrexian")).hasSize(1);
        assertThat(findPermanents(player2, "Phyrexian")).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    private Permanent addSaga(Player player, int loreCounters) {
        Permanent saga = new Permanent(new ThePhasingOfZhalfir());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player.getId()).add(saga);
        return saga;
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        return addCreatureReady(player, card);
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
