package com.github.laxika.magicalvibes.cards.j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({JudgeUnworthy.class, Forest.class, GrizzlyBears.class})
class JudgeUnworthyTest extends BaseCardTest {

    @Test
    @DisplayName("Scries 3, then deals damage equal to the revealed card's mana value")
    void scriesThenDamagesAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), topCard));

        castJudgeUnworthy(attacker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void canTargetBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        castJudgeUnworthy(blocker);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JudgeUnworthy()));
        addJudgeUnworthyMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private void castJudgeUnworthy(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JudgeUnworthy()));
        addJudgeUnworthyMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addJudgeUnworthyMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
