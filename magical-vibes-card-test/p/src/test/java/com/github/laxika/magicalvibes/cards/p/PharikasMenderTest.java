package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.o.OrdealOfNylea;
import com.github.laxika.magicalvibes.cards.s.SavageSurge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PharikasMender.class, NessianCourser.class, OrdealOfNylea.class, SavageSurge.class})
class PharikasMenderTest extends BaseCardTest {

    private void castMender() {
        harness.castFromHand(player1, new PharikasMender(), "{3}{B}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB offers creature and enchantment cards as targets")
    void etbPromptsForCreatureOrEnchantment() {
        Card creature = new NessianCourser();
        Card enchantment = new OrdealOfNylea();
        harness.setGraveyard(player1, List.of(creature, enchantment, new SavageSurge()));

        castMender();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
    }

    @Test
    @DisplayName("Returns the chosen creature or enchantment to hand")
    void returnsChosenCardToHand() {
        Card creature = new NessianCourser();
        Card enchantment = new OrdealOfNylea();
        harness.setGraveyard(player1, List.of(creature, enchantment));

        castMender();
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Ordeal of Nylea");
        harness.assertInGraveyard(player1, "Nessian Courser");
    }

    @Test
    @DisplayName("A legal target must be selected even when the controller intends to decline")
    void cannotChooseZeroTargets() {
        Card creature = new NessianCourser();
        harness.setGraveyard(player1, List.of(creature));

        castMender();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Nessian Courser");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller can decline the return when the targeted ability resolves")
    void canDeclineReturnAtResolution() {
        Card creature = new NessianCourser();
        harness.setGraveyard(player1, List.of(creature));

        castMender();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Nessian Courser");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns a targeted creature when the controller accepts")
    void returnsCreatureToHand() {
        Card creature = new NessianCourser();
        harness.setGraveyard(player1, List.of(creature));

        castMender();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Nessian Courser");
        harness.assertNotInGraveyard(player1, "Nessian Courser");
    }

    @Test
    @DisplayName("Cards in the opponent's graveyard are not offered as targets")
    void onlyTargetsControllersGraveyard() {
        Card ownCreature = new NessianCourser();
        Card opposingCreature = new NessianCourser();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        castMender();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
    }

    @Test
    @DisplayName("A target that leaves the graveyard before resolution is not returned")
    void removedTargetIsNotReturned() {
        Card creature = new NessianCourser();
        harness.setGraveyard(player1, List.of(creature));

        castMender();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Non-creature non-enchantment cards are not valid targets")
    void nonMatchingCardsAreNotTargets() {
        Card instant = new SavageSurge();
        harness.setGraveyard(player1, List.of(instant));

        castMender();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Savage Surge");
    }
}
