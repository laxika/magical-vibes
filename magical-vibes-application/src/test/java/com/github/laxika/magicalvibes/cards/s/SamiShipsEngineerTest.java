package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamiShipsEngineer.class, GrizzlyBears.class})
class SamiShipsEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a tapped 2/2 colorless Robot artifact creature with two tapped creatures")
    void createsTappedRobotWithTwoTappedCreatures() {
        harness.addToBattlefield(player1, new SamiShipsEngineer());
        addTappedCreature(player1);
        addTappedCreature(player1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.isTapped()).isTrue();
        assertThat(robot.getCard().isToken()).isTrue();
        assertThat(robot.getCard().getColor()).isNull();
        assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when fewer than two creatures are tapped under your control")
    void doesNotTriggerWithoutTwoTappedCreatures() {
        harness.addToBattlefield(player1, new SamiShipsEngineer());
        addTappedCreature(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).getFirst().tap();

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Robot")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rechecks the tapped-creature condition when the trigger resolves")
    void rechecksConditionOnResolution() {
        harness.addToBattlefield(player1, new SamiShipsEngineer());
        Permanent first = addTappedCreature(player1);
        addTappedCreature(player1);

        advanceToEndStep(player1);
        first.untap();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.tap();
        return creature;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
