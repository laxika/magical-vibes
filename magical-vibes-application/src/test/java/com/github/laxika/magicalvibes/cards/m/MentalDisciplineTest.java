package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.q.Quash;
import com.github.laxika.magicalvibes.cards.y.YavimayaEnchantress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MentalDiscipline.class, YavimayaEnchantress.class, Quash.class})
class MentalDisciplineTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and paying mana draws a card")
    void discardsAndDraws() {
        harness.addToBattlefield(player1, new MentalDiscipline());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new YavimayaEnchantress()));
        harness.setLibrary(player1, List.of(new Quash()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Quash");
        harness.assertInGraveyard(player1, "Yavimaya Enchantress");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new MentalDiscipline());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new MentalDiscipline());
        harness.setHand(player1, List.of(new YavimayaEnchantress()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not tap and can be activated more than once per turn")
    void canBeActivatedRepeatedlyWithoutTapping() {
        harness.addToBattlefield(player1, new MentalDiscipline());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new YavimayaEnchantress(), new YavimayaEnchantress()));
        harness.setLibrary(player1, List.of(new Quash(), new Quash()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Quash", "Quash");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Yavimaya Enchantress", "Yavimaya Enchantress");
    }
}
