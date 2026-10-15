package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.w.WizardClass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnbringerCleric.class, HillGiantHerdgorger.class, WizardClass.class})
class DawnbringerClericTest extends BaseCardTest {

    @Test
    void cureWoundsGainsTwoLife() {
        int lifeBefore = gd.getLife(player1.getId());
        castCleric();

        harness.handleListChoice(player1, "Cure Wounds — You gain 2 life.");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void dispelMagicDestroysTargetEnchantment() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new WizardClass());
        castCleric();

        harness.handleListChoice(player1, "Dispel Magic — Destroy target enchantment.");
        harness.handlePermanentChosen(player1, anthem.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wizard Class");
    }

    @Test
    void gentleReposeExilesTargetGraveyardCard() {
        Card graveyardCard = new HillGiantHerdgorger();
        harness.setGraveyard(player2, List.of(graveyardCard));
        castCleric();

        harness.handleListChoice(player1, "Gentle Repose — Exile target card from a graveyard.");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
    }

    @Test
    void dispelMagicCanDestroyYourOwnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        castCleric();

        harness.handleListChoice(player1, "Dispel Magic — Destroy target enchantment.");
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wizard Class");
        harness.assertInGraveyard(player1, "Wizard Class");
        harness.assertLife(player1, 20);
    }

    @Test
    void gentleReposeCanExileYourOwnNoncreatureCardAndLeavesOtherCards() {
        Card target = new WizardClass();
        Card other = new HillGiantHerdgorger();
        harness.setGraveyard(player1, List.of(target, other));
        castCleric();

        harness.handleListChoice(player1, "Gentle Repose — Exile target card from a graveyard.");
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertLife(player1, 20);
    }

    @Test
    void gentleReposeCannotBeChosenWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        castCleric();

        PendingInteraction.ColorChoice choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).contains("Cure Wounds — You gain 2 life.")
                .doesNotContain("Gentle Repose — Exile target card from a graveyard.");
    }

    @Test
    void gentleReposeDoesNotExileAnotherCardIfTargetLeavesGraveyard() {
        Card target = new WizardClass();
        Card other = new HillGiantHerdgorger();
        harness.setGraveyard(player2, List.of(target, other));
        castCleric();

        harness.handleListChoice(player1, "Gentle Repose — Exile target card from a graveyard.");
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(other));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target, other);
        harness.assertLife(player1, 20);
    }

    private void castCleric() {
        harness.setHand(player1, List.of(new DawnbringerCleric()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }
}
