package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DreadfulApathy;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchonOfFallingStars.class, AuraOfSilence.class, GrizzlyBears.class, WrathOfGod.class,
        DreadfulApathy.class})
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
        resolveReturnChoice(true);

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
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        resolveReturnChoice(false);

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

    @Test
    @DisplayName("A target is required even when the controller intends to decline the return")
    void requiresTargetBeforeOptionalReturn() {
        Card enchantment = new AuraOfSilence();
        harness.addToBattlefield(player1, new ArchonOfFallingStars());
        harness.setGraveyard(player1, List.of(enchantment));

        destroyArchon();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("An Aura returned from the graveyard must enter attached to a chosen legal creature")
    void returnsAuraAttachedToChosenCreature() {
        Card aura = new DreadfulApathy();
        harness.addToBattlefield(player1, new ArchonOfFallingStars());
        harness.setGraveyard(player1, List.of(aura));

        destroyArchon();
        Permanent host = harness.addToBattlefieldAndReturn(player2, new ArchonOfFallingStars());
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, host.getId());

        harness.assertOnBattlefield(player1, "Dreadful Apathy");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(aura.getId())).findFirst().orElseThrow()
                .getAttachedTo()).isEqualTo(host.getId());
    }

    @Test
    @DisplayName("An opponent's enchantment cannot be targeted")
    void cannotTargetOpponentsGraveyard() {
        Card ownEnchantment = new AuraOfSilence();
        Card opponentsEnchantment = new AuraOfSilence();
        harness.addToBattlefield(player1, new ArchonOfFallingStars());
        harness.setGraveyard(player1, List.of(ownEnchantment));
        harness.setGraveyard(player2, List.of(opponentsEnchantment));

        destroyArchon();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownEnchantment.getId());
    }

    private void resolveReturnChoice(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, accepted);
        harness.passBothPriorities();
    }

    private void destroyArchon() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
