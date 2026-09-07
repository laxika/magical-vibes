package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchonOfFallingStars.class, AuraOfSilence.class, GrizzlyBears.class, WrathOfGod.class})
class ArchonOfFallingStarsTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it returns a target enchantment card to the battlefield")
    void returnsTargetEnchantmentToBattlefield() {
        ArchonOfFallingStars archon = new ArchonOfFallingStars();
        Card enchantment = new AuraOfSilence();
        harness.addToBattlefield(player1, archon);
        harness.setGraveyard(player1, List.of(enchantment));

        destroyArchon();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aura of Silence");
        harness.assertInGraveyard(player1, "Archon of Falling Stars");
    }

    @Test
    @DisplayName("The death trigger can be declined")
    void canDeclineReturningEnchantment() {
        Card enchantment = new AuraOfSilence();
        harness.addToBattlefield(player1, new ArchonOfFallingStars());
        harness.setGraveyard(player1, List.of(enchantment));

        destroyArchon();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aura of Silence");
        harness.assertInGraveyard(player1, "Archon of Falling Stars");
    }

    @Test
    @DisplayName("Only enchantment cards are legal death-trigger targets")
    void onlyEnchantmentsAreLegalTargets() {
        Card creature = new GrizzlyBears();
        Card enchantment = new AuraOfSilence();
        harness.addToBattlefield(player1, new ArchonOfFallingStars());
        harness.setGraveyard(player1, List.of(creature, enchantment));

        destroyArchon();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
    }

    private void destroyArchon() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
