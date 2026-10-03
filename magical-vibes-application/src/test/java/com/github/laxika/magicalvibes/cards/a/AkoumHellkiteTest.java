package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkoumHellkite.class, Forest.class, Mountain.class, SmiteTheMonstrous.class})
class AkoumHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when a Mountain enters under your control")
    void mountainLandfallDealsTwoDamage() {
        harness.addToBattlefield(player1, new AkoumHellkite());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 1 damage when a non-Mountain land enters under your control")
    void nonMountainLandfallDealsOneDamage() {
        harness.addToBattlefield(player1, new AkoumHellkite());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Landfall can target Akoum Hellkite itself")
    void canTargetItself() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new AkoumHellkite());
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, hellkite.getId());
        harness.passBothPriorities();

        assertThat(hellkite.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Akoum Hellkite");
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentsLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new AkoumHellkite());
        harness.setHand(player2, List.of(new Mountain()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Landfall can damage its controller")
    void canTargetController() {
        harness.addToBattlefield(player1, new AkoumHellkite());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Landfall resolves after Akoum Hellkite is destroyed")
    void triggerResolvesAfterSourceLeaves() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new AkoumHellkite());
        harness.setHand(player1, List.of(new Mountain(), new SmiteTheMonstrous()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, hellkite.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Akoum Hellkite");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
