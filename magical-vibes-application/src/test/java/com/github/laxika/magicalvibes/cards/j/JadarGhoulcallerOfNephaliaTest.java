package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadarGhoulcallerOfNephalia.class, GrizzlyBears.class})
class JadarGhoulcallerOfNephaliaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a decayed Zombie at the beginning of your end step when you control none")
    void createsDecayedZombieWithoutAnotherDecayedCreature() {
        harness.addToBattlefield(player1, new JadarGhoulcallerOfNephalia());

        advanceToControllerEndStep();
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
    }

    @Test
    @DisplayName("Does not create a Zombie while you control a creature with decayed")
    void doesNotCreateWithAnotherDecayedCreature() {
        harness.addToBattlefield(player1, new JadarGhoulcallerOfNephalia());
        GrizzlyBears decayedCreature = new GrizzlyBears();
        decayedCreature.setKeywords(Set.of(Keyword.DECAYED));
        harness.addToBattlefield(player1, decayedCreature);

        advanceToControllerEndStep();

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
