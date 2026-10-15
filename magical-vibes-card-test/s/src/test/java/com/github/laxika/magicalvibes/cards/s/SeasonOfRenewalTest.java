package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GenerousVisitor;
import com.github.laxika.magicalvibes.cards.f.FavorOfJukai;
import com.github.laxika.magicalvibes.cards.j.JukaiNaturalist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeasonOfRenewal.class, GenerousVisitor.class, FavorOfJukai.class, JukaiNaturalist.class})
class SeasonOfRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode returns a creature card to hand")
    void creatureModeReturnsCreature() {
        Card creature = new GenerousVisitor();
        Card enchantment = new FavorOfJukai();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        castSeasonOfRenewal(0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, choice.validCardIds());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Generous Visitor");
        harness.assertInGraveyard(player1, "Favor of Jukai");
    }

    @Test
    @DisplayName("Enchantment mode returns an enchantment card to hand")
    void enchantmentModeReturnsEnchantment() {
        Card creature = new GenerousVisitor();
        Card enchantment = new FavorOfJukai();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        castSeasonOfRenewal(1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, choice.validCardIds());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Favor of Jukai");
        harness.assertInGraveyard(player1, "Generous Visitor");
    }

    @Test
    @DisplayName("Both mode returns a creature and an enchantment card")
    void bothModeReturnsBothCards() {
        Card creature = new GenerousVisitor();
        Card enchantment = new FavorOfJukai();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        castSeasonOfRenewal(2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, choice.validCardIds());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Generous Visitor");
        harness.assertInHand(player1, "Favor of Jukai");
    }

    @Test
    @DisplayName("Each single mode requires a target")
    void singleModesCannotChooseZeroTargets() {
        Card creature = new GenerousVisitor();
        harness.setGraveyard(player1, List.of(creature));
        castSeasonOfRenewal(0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchantment mode requires a target")
    void enchantmentModeCannotChooseZeroTargets() {
        harness.setGraveyard(player1, List.of(new FavorOfJukai()));
        castSeasonOfRenewal(1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both modes cannot target two creature-only cards")
    void bothModesRejectTwoCreatures() {
        Card first = new GenerousVisitor();
        Card second = new GenerousVisitor();
        harness.setGraveyard(player1, List.of(first, second, new FavorOfJukai()));
        castSeasonOfRenewal(2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both modes cannot target two enchantment-only cards")
    void bothModesRejectTwoEnchantments() {
        Card first = new FavorOfJukai();
        Card second = new FavorOfJukai();
        harness.setGraveyard(player1, List.of(first, second, new GenerousVisitor()));
        castSeasonOfRenewal(2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both modes require a target for each mode")
    void bothModesCannotOmitEnchantmentTarget() {
        Card creature = new GenerousVisitor();
        harness.setGraveyard(player1, List.of(creature, new FavorOfJukai()));
        castSeasonOfRenewal(2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An enchantment creature can be targeted by both modes")
    void bothModesCanTargetSameEnchantmentCreature() {
        Card creature = new JukaiNaturalist();
        harness.setGraveyard(player1, List.of(creature));
        castSeasonOfRenewal(2);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        harness.assertNotInGraveyard(player1, "Jukai Naturalist");
    }

    private void castSeasonOfRenewal(int mode) {
        harness.setHand(player1, List.of(new SeasonOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstant(player1, 0, mode, List.of());
    }
}
