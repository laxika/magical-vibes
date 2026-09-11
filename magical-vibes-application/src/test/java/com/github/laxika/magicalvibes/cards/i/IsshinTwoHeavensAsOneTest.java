package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.j.JovensFerrets;
import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IsshinTwoHeavensAsOne.class, JovensFerrets.class, Roterothopter.class})
class IsshinTwoHeavensAsOneTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles attack triggers of permanents you control")
    void doublesAttackTriggers() {
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(ferrets.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not double non-attack triggers")
    void doesNotDoubleNonAttackTriggers() {
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());
        ferrets.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Roterothopter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
    }
}
