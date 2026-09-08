package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WizardMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself and a creature you control to their owners' hands")
    void returnsSelfAndControlledCreature() {
        Permanent mentor = addCreatureReady(player1, new WizardMentor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wizard Mentor");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Wizard Mentor");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(mentor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        addCreatureReady(player1, new WizardMentor());
        Permanent mentor = findPermanent(player1, "Wizard Mentor");

        harness.activateAbility(player1, 0, null, mentor.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wizard Mentor");
        harness.assertNotOnBattlefield(player1, "Wizard Mentor");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new WizardMentor());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
