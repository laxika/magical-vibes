package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearslicerGoblin.class})
class SearslicerGoblinTest extends BaseCardTest {

    @Test
    void createsGoblinTokenAtEndStepAfterAttacking() {
        harness.addToBattlefield(player1, new SearslicerGoblin());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToEndStep(player1);

        assertThat(goblinTokenCount()).isEqualTo(1);
        var token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    void doesNotCreateGoblinTokenWithoutAttacking() {
        harness.addToBattlefield(player1, new SearslicerGoblin());

        advanceToEndStep(player1);

        assertThat(goblinTokenCount()).isZero();
    }

    @Test
    void opponentAttackDoesNotEnableRaid() {
        harness.addToBattlefield(player1, new SearslicerGoblin());
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        advanceToEndStep(player1);

        assertThat(goblinTokenCount()).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStepEvenWithRaidEnabled() {
        harness.addToBattlefield(player1, new SearslicerGoblin());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToEndStep(player2);

        assertThat(goblinTokenCount()).isZero();
    }

    @Test
    void eachCopyCreatesOneToken() {
        harness.addToBattlefield(player1, new SearslicerGoblin());
        harness.addToBattlefield(player1, new SearslicerGoblin());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(goblinTokenCount()).isEqualTo(2);
    }

    @Test
    void triggersEvenIfGoblinEnteredAfterTheAttack() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.addToBattlefield(player1, new SearslicerGoblin());

        advanceToEndStep(player1);

        assertThat(goblinTokenCount()).isEqualTo(1);
    }

    @Test
    void triggeredAbilityResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new SearslicerGoblin());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        var source = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        assertThat(goblinTokenCount()).isEqualTo(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private long goblinTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Goblin"))
                .count();
    }
}
