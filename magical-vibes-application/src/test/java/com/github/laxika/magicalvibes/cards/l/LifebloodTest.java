package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lifeblood.class, Mountain.class, Island.class})
class LifebloodTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's Mountain becoming tapped gains the controller 1 life")
    void opponentMountainTapGainsLife() {
        harness.addToBattlefield(player1, new Lifeblood());
        harness.addToBattlefield(player2, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Each time an opponent's Mountain becomes tapped, the controller gains 1 life")
    void eachOpponentMountainTapTriggersSeparately() {
        harness.addToBattlefield(player1, new Lifeblood());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();
        mountain.untap();
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Tapping your own Mountain does not trigger")
    void ownMountainTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifeblood());
        harness.addToBattlefield(player1, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player1, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping an opponent's non-Mountain land does not trigger")
    void opponentNonMountainTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifeblood());
        harness.addToBattlefield(player2, new Island());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.tapPermanent(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

}
