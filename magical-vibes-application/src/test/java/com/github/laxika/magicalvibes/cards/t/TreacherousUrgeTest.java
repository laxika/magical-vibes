package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreacherousUrge.class, GrizzlyBears.class, Forest.class})
class TreacherousUrgeTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a creature from the target opponent's hand and puts it under the caster's control")
    void choosesCreatureFromTargetOpponentsHand() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        castTreacherousUrge();

        PendingInteraction.TargetedHandBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.TargetedHandBattlefieldChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(bears.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("Sacrifices the chosen creature at the next end step")
    void sacrificesChosenCreatureAtNextEndStep() {
        Card opponentCreature = new GrizzlyBears();
        opponentCreature.setOwnerId(player2.getId());
        harness.setHand(player2, List.of(opponentCreature));
        castTreacherousUrge();
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the target hand unchanged")
    void decliningLeavesTargetHandUnchanged() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castTreacherousUrge();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void canTargetOnlyOpponent() {
        harness.setHand(player1, List.of(new TreacherousUrge()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTreacherousUrge() {
        harness.setHand(player1, List.of(new TreacherousUrge()));
        addManaForSpell();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
