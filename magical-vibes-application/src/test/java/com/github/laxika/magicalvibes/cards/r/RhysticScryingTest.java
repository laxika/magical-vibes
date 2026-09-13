package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RhysticScrying.class)
class RhysticScryingTest extends BaseCardTest {

    @Test
    void drawsThreeAndDoesNotDiscardWhenNoPlayerPays() {
        harness.setLibrary(player1, List.of(new RhysticScrying(), new RhysticScrying(), new RhysticScrying()));
        castScrying();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anyPlayerCanPayToMakeTheControllerDiscardThree() {
        harness.setLibrary(player1, List.of(new RhysticScrying(), new RhysticScrying(), new RhysticScrying()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        castScrying();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void controllerCanPayToMakeTheControllerDiscardThree() {
        harness.setLibrary(player1, List.of(new RhysticScrying(), new RhysticScrying(), new RhysticScrying()));
        castScrying();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castScrying() {
        harness.castFromHand(player1, new RhysticScrying(), "{2}{U}{U}");
        harness.passBothPriorities();
    }
}
