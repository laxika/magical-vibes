package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({HeavenlyQilin.class, GrizzlyBears.class})
class HeavenlyQilinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking lets the trigger target another creature you control")
    void attackTriggerTargetsAnotherCreatureYouControl() {
        Permanent qilin = addReadyCreature(player1, new HeavenlyQilin());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreature(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        assertThat(choice.validIds()).doesNotContain(qilin.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("The attack trigger grants flying until end of turn")
    void attackTriggerGrantsFlyingUntilEndOfTurn() {
        addReadyCreature(player1, new HeavenlyQilin());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger rejects the attacking source as a target")
    void attackTriggerRejectsSource() {
        Permanent qilin = addReadyCreature(player1, new HeavenlyQilin());
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, qilin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger still resolves after Qilin leaves the battlefield")
    void attackTriggerResolvesWithoutSource() {
        Permanent qilin = addReadyCreature(player1, new HeavenlyQilin());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(qilin);
        gd.playerGraveyards.get(player1.getId()).add(qilin.getCard());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A target that changes controllers before resolution does not gain flying")
    void attackTriggerDoesNotAffectTargetNowControlledByOpponent() {
        addReadyCreature(player1, new HeavenlyQilin());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the target does not grant flying to a replacement creature")
    void attackTriggerDoesNotAffectReplacementCreature() {
        addReadyCreature(player1, new HeavenlyQilin());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        Permanent replacement = addReadyCreature(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
