package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IntrepidAce.class)
class IntrepidAceTest extends BaseCardTest {

    @Test
    @DisplayName("Intrepid Ace gets +2/+0 while it is neither attacking nor blocking")
    void boostsWhileNotAttackingOrBlocking() {
        Permanent ace = addCreatureReady(player1, new IntrepidAce());

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ace)).isEqualTo(1);
    }

    @Test
    @DisplayName("Intrepid Ace loses its boost while attacking")
    void losesBoostWhileAttacking() {
        Permanent ace = addCreatureReady(player1, new IntrepidAce());
        ace.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(2);
    }

    @Test
    @DisplayName("Intrepid Ace loses its boost while blocking")
    void losesBoostWhileBlocking() {
        Permanent ace = addCreatureReady(player1, new IntrepidAce());
        ace.setBlocking(true);

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(2);
    }

    @Test
    @DisplayName("Intrepid Ace deals two combat damage and regains its boost after combat")
    void dealsTwoDamageAndRegainsBoostAfterCombat() {
        Permanent ace = addCreatureReady(player1, new IntrepidAce());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(List.of(0));
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(2);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(4);
    }

    @Test
    @DisplayName("An idle Intrepid Ace keeps its boost when another Intrepid Ace attacks")
    void combatConditionAppliesOnlyToEachIndividualAce() {
        Permanent attacker = addCreatureReady(player1, new IntrepidAce());
        Permanent idle = addCreatureReady(player1, new IntrepidAce());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, idle)).isEqualTo(4);
    }
}
