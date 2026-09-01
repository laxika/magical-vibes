package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LagonnaBandStoryteller.class, Pacifism.class, GrizzlyBears.class})
class LagonnaBandStorytellerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put a target enchantment card on top of the library and gain its mana value")
    void putsEnchantmentOnTopAndGainsLife() {
        Card enchantment = new Pacifism();
        Card nonEnchantment = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonEnchantment, enchantment));
        harness.setLibrary(player1, List.of());

        castStoryteller();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(enchantment.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Declining the ETB leaves the targeted enchantment card in the graveyard")
    void decliningLeavesEnchantmentInGraveyard() {
        Pacifism enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setLibrary(player1, List.of());

        castStoryteller();

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("ETB does not target non-enchantment cards")
    void doesNotTargetNonEnchantment() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of());

        castStoryteller();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castStoryteller() {
        harness.setHand(player1, List.of(new LagonnaBandStoryteller()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
