package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuthorityOfTheConsuls.class, GrizzlyBears.class})
class AuthorityOfTheConsulsTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creatures enter tapped and Authority's controller gains life")
    void opponentsCreaturesEnterTappedAndGainLife() {
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller's creatures enter untapped and do not cause life gain")
    void controllersCreaturesEnterUntappedWithoutLifeGain() {
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Life gain waits for the trigger to resolve")
    void lifeGainUsesTheStack() {
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(bears.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Authority gains life independently for the same creature")
    void multipleAuthoritiesEachTrigger() {
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.setLife(player1, 20);

        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(bears.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Opponent's noncreature permanents enter untapped without life gain")
    void noncreaturePermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.setLife(player1, 20);

        Permanent authority = harness.enterBattlefieldAndReturn(player2, new AuthorityOfTheConsuls());

        assertThat(authority.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
