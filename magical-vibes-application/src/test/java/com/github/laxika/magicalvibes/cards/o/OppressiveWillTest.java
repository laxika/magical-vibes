package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OppressiveWill.class, HandOfHonor.class})
class OppressiveWillTest extends BaseCardTest {

    @Test
    void countersWhenTargetControllerCannotPayCardsInControllerHand() {
        HandOfHonor handOfHonor = new HandOfHonor();
        harness.setHand(player1, List.of(handOfHonor));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new OppressiveWill(), new HandOfHonor(), new HandOfHonor()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, handOfHonor.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hand of Honor");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void targetControllerCanPayDynamicCostBasedOnOppressiveWillControllerHand() {
        HandOfHonor handOfHonor = new HandOfHonor();
        harness.setHand(player1, List.of(handOfHonor, new HandOfHonor()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(
                new OppressiveWill(), new HandOfHonor(), new HandOfHonor(), new HandOfHonor()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, handOfHonor.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hand of Honor");
    }

    @Test
    void targetControllerMayDeclineToPayDynamicCost() {
        HandOfHonor handOfHonor = new HandOfHonor();
        harness.setHand(player1, List.of(handOfHonor, new HandOfHonor()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new OppressiveWill(), new HandOfHonor()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, handOfHonor.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hand of Honor");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void cannotTargetPermanent() {
        HandOfHonor handOfHonor = new HandOfHonor();
        harness.addToBattlefield(player1, handOfHonor);

        harness.setHand(player2, List.of(new OppressiveWill()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, handOfHonor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
