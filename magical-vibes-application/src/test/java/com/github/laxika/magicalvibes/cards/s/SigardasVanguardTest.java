package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigardasVanguard.class, GrizzlyBears.class, LlanowarElves.class})
class SigardasVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives double strike to any number of creatures with different powers")
    void etbGrantsDoubleStrikeToDistinctPowers() {
        Permanent bears = addReadyCreature(new GrizzlyBears());
        Permanent elves = addReadyCreature(new LlanowarElves());
        Permanent vanguard = castVanguard();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                bears.getId(), elves.getId(), vanguard.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(bears.getId(), elves.getId(), vanguard.getId()));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Rejects a selection containing creatures with the same power")
    void rejectsDuplicatePowers() {
        Permanent firstBears = addReadyCreature(new GrizzlyBears());
        Permanent secondBears = addReadyCreature(new GrizzlyBears());
        castVanguard();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    @DisplayName("Attack trigger grants double strike until end of turn")
    void attackTriggerGrantsUntilEndOfTurn() {
        Permanent vanguard = addReadyCreature(new SigardasVanguard());
        Permanent bears = addReadyCreature(new GrizzlyBears());
        Permanent elves = addReadyCreature(new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(vanguard)));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(vanguard.getId(), bears.getId(), elves.getId()));

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent castVanguard() {
        harness.setHand(player1, List.of(new SigardasVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return find(player1, "Sigarda's Vanguard");
    }

    private Permanent addReadyCreature(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
