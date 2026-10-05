package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfObligation.class, GreensideWatcher.class})
class KnightOfObligationTest extends BaseCardTest {

    @Test
    void payingExtortDrainsEachOpponent() {
        harness.addToBattlefield(player1, new KnightOfObligation());
        harness.setHand(player1, List.of(new GreensideWatcher()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void decliningExtortDoesNothing() {
        harness.addToBattlefield(player1, new KnightOfObligation());
        harness.setHand(player1, List.of(new GreensideWatcher()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player1, new KnightOfObligation());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GreensideWatcher()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void blackManaCanPayExtort() {
        harness.addToBattlefield(player1, new KnightOfObligation());
        harness.setHand(player1, List.of(new GreensideWatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void castingKnightDoesNotTriggerItsOwnExtort() {
        harness.setHand(player1, List.of(new KnightOfObligation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Knight of Obligation");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void attackingDoesNotTapKnight() {
        var knight = addCreatureReady(player1, new KnightOfObligation());

        declareAttackers(List.of(0));

        assertThat(knight.isAttacking()).isTrue();
        assertThat(knight.isTapped()).isFalse();
    }
}
