package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChieftainEnDal.class, DefiantFalcon.class})
class ChieftainEnDalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Chieftain en-Dal grants first strike to all attacking creatures")
    void attackGrantsFirstStrikeToAttackingCreatures() {
        Permanent chieftain = addCreatureReady(player1, new ChieftainEnDal());
        Permanent attacker = addCreatureReady(player1, new DefiantFalcon());
        Permanent nonAttacker = addCreatureReady(player1, new DefiantFalcon());
        Permanent opponentCreature = addCreatureReady(player2, new DefiantFalcon());

        attackWithChieftainAndCreature();

        assertThat(gqs.hasKeyword(gd, chieftain, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike granted by Chieftain en-Dal wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ChieftainEnDal());
        Permanent attacker = addCreatureReady(player1, new DefiantFalcon());

        attackWithChieftainAndCreature();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant first strike when Chieftain en-Dal does not attack")
    void doesNotGrantFirstStrikeWhenChieftainDoesNotAttack() {
        Permanent chieftain = addCreatureReady(player1, new ChieftainEnDal());
        Permanent attacker = addCreatureReady(player1, new DefiantFalcon());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chieftain, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void attackWithChieftainAndCreature() {
        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
    }
}
