package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gingerbrute.class, GrizzlyBears.class, RagingGoblin.class})
class GingerbruteTest extends BaseCardTest {

    @Test
    @DisplayName("The ability prevents non-haste creatures from blocking this turn")
    void nonHasteCreatureCannotBlock() {
        Permanent gingerbrute = addReadyGingerbrute();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        activateEvasionAbility(gingerbrute);
        gingerbrute.setAttacking(true);
        beginDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, gingerbrute))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("haste");
    }

    @Test
    @DisplayName("The ability allows a creature with haste to block this turn")
    void hasteCreatureCanBlock() {
        Permanent gingerbrute = addReadyGingerbrute();
        Permanent blocker = addCreatureReady(player2, new RagingGoblin());
        activateEvasionAbility(gingerbrute);
        gingerbrute.setAttacking(true);
        beginDeclareBlockers();

        declareBlock(blocker, gingerbrute);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at end of turn")
    void evasionExpiresAtEndOfTurn() {
        Permanent gingerbrute = addReadyGingerbrute();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        activateEvasionAbility(gingerbrute);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        gingerbrute.setAttacking(true);
        beginDeclareBlockers();
        declareBlock(blocker, gingerbrute);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The sacrifice ability gains 3 life")
    void sacrificeAbilityGainsLife() {
        addReadyGingerbrute();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    private Permanent addReadyGingerbrute() {
        return addCreatureReady(player1, new Gingerbrute());
    }

    private void activateEvasionAbility(Permanent gingerbrute) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(gingerbrute),
                0, null, null);
        harness.passBothPriorities();
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
