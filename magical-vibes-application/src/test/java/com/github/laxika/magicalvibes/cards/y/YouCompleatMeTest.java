package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouCompleatMe.class})
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
        harness.setLibrary(player1, List.of(new YouCompleatMe()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(9);
        harness.assertInHand(player1, "You Compleat Me");
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
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emblemDoesNotTriggerDuringOpponentsUpkeep() {
        harness.setLibrary(player1, List.of(new YouCompleatMe()));
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void multipleEmblemsEachDrawAndLoseLife() {
        harness.setLibrary(player1, List.of(new YouCompleatMe(), new YouCompleatMe()));
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 8);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateEmblemWithLessThanTwoLife() {
        harness.setLife(player1, 1);
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateEmblemAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void emblemCanBeActivatedRepeatedlyWithoutUsingTheStack() {
        harness.castFromHand(player1, new YouCompleatMe(), "{1}{B}{B}");
        harness.passBothPriorities();

        harness.activateEmblemAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        harness.activateEmblemAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        harness.assertLife(player1, 6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
