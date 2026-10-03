package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoulderDash.class, GrizzlyBears.class, GiantSpider.class})
class BoulderDashTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToFirstTargetAndOneDamageToSecondTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(bears.getId(), spider.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    void dealsTwoDamageToFirstTargetAndOneDamageToSecondPlayerTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(bears.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotCastWithDuplicateTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    void canDealTwoDamageToOpponentAndOneToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Boulder Dash");
    }

    @Test
    void marksExactOrderedDamageOnSurvivingCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    void cannotCastWithOnlyOneTarget() {
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithThreeTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(bears.getId(), player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondTargetStillTakesOnlyOneDamageWhenFirstTargetLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(bears.getId(), player2.getId()));
        harness.getPermanentRemovalService().removePermanentToHand(harness.getGameData(), bears);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Boulder Dash");
    }

    @Test
    void firstTargetStillTakesTwoDamageWhenSecondTargetLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), bears.getId()));
        harness.getPermanentRemovalService().removePermanentToHand(harness.getGameData(), bears);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void doesNotResolveWhenBothTargetsLeave() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(bears.getId(), spider.getId()));
        harness.getPermanentRemovalService().removePermanentToHand(harness.getGameData(), bears);
        harness.getPermanentRemovalService().removePermanentToHand(harness.getGameData(), spider);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Boulder Dash");
    }

}
