package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimmerSeeker.class, GrizzlyBears.class})
class GlimmerSeekerTest extends BaseCardTest {

    @Test
    void createsGlimmerTokenWithoutControlledGlimmerCreature() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glimmer");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsCardWithControlledGlimmerCreature() {
        addGlimmerCreature();
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());
        seeker.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> "Glimmer".equals(permanent.getCard().getName()))
                .hasSize(1);
    }

    @Test
    void doesNotTriggerWhenUntapped() {
        harness.addToBattlefieldAndReturn(player1, new GlimmerSeeker());

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addGlimmerCreature() {
        Card glimmer = new Card();
        glimmer.setName("Glimmer");
        glimmer.setType(CardType.CREATURE);
        glimmer.setSubtypes(List.of(CardSubtype.GLIMMER));
        glimmer.setPower(1);
        glimmer.setToughness(1);
        return harness.addToBattlefieldAndReturn(player1, glimmer);
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
