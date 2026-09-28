package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouCompleatMe.class, GrizzlyBears.class})
class YouCompleatMeTest extends BaseCardTest {

    @Test
    void setsAndEnforcesMaximumLifeTotal() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getMaximumLifeTotal(player1.getId())).isEqualTo(10);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyGainLife(gd, player1.getId(), 5));

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    void doesNotLowerLifeBelowTen() {
        harness.setLife(player1, 5);
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(5);
        assertThat(gd.getMaximumLifeTotal(player1.getId())).isEqualTo(10);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyGainLife(gd, player1.getId(), 10));

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    void emblemDrawsAndLosesLifeAtUpkeep() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(9);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void emblemPaysLifeForManaOfAnyColor() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();

        harness.activateEmblemAbility(player1, 0, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
