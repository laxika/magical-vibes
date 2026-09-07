package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Compulsion.class, GrizzlyBears.class, Mountain.class})
class CompulsionTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card draws a card")
    void discardingCardDrawsCard() {
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing Compulsion draws a card")
    void sacrificingCompulsionDrawsCard() {
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Compulsion);
        harness.assertInGraveyard(player1, "Compulsion");
    }

    @Test
    @DisplayName("The discard ability cannot be activated with an empty hand")
    void discardAbilityRequiresCardInHand() {
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
