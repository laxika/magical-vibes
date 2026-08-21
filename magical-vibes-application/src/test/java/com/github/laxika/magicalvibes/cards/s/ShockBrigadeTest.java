package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ShockBrigade.class)
class ShockBrigadeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking red Warrior token")
    void attackingCreatesTappedAndAttackingWarriorToken() {
        addCreatureReady(player1, new ShockBrigade());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
        assertThat(tokens.getFirst().isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The mobilized token is sacrificed at the beginning of the next end step")
    void mobilizedTokenIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new ShockBrigade());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isOne();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }
}
