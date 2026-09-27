package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Fleshgrafter;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltronTheAnnihilator.class, Fleshgrafter.class, LeoninScimitar.class})
class UltronTheAnnihilatorTest extends BaseCardTest {

    @Test
    void entersAndAttacksToCreateRobotTokens() {
        addUltronReady();
        resolveAllTriggers();

        assertThat(robotTokens()).hasSize(1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(robotTokens()).hasSize(2);
        assertThat(robotTokens()).allSatisfy(token ->
                assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue());
    }

    @Test
    void artifactPermanentInOwnGraveyardMakesEachOpponentLoseLife() {
        harness.setLife(player2, 20);
        addUltronReady();
        resolveAllTriggers();
        Permanent robot = robotTokens().getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, robot));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void artifactCardDiscardedFromHandMakesEachOpponentLoseLife() {
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addUltronReady();
        resolveAllTriggers();
        harness.addToBattlefieldAndReturn(player1, new Fleshgrafter());
        harness.setHand(player1, List.of(new LeoninScimitar()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Permanent addUltronReady() {
        Permanent ultron = harness.addToBattlefieldAndReturn(player1, new UltronTheAnnihilator());
        ultron.setSummoningSick(false);
        return ultron;
    }

    private List<Permanent> robotTokens() {
        return findPermanents(player1, "Robot").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
