package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SionaCaptainOfThePyleas.class, HolyStrength.class, GrizzlyBears.class})
class SionaCaptainOfThePyleasTest extends BaseCardTest {

    @Test
    @DisplayName("Siona may put an Aura from the top seven cards into its controller's hand")
    void entersAndOffersAuraFromTopSeven() {
        Card aura = new HolyStrength();
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GrizzlyBears();
        Card creature3 = new GrizzlyBears();
        Card creature4 = new GrizzlyBears();
        Card creature5 = new GrizzlyBears();
        Card creature6 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(aura, creature1, creature2, creature3, creature4, creature5, creature6));
        harness.setHand(player1, List.of(new SionaCaptainOfThePyleas()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(
                aura, creature1, creature2, creature3, creature4, creature5, creature6);
        assertThat(choice.validCardIds()).containsExactly(aura.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creature1, creature2, creature3, creature4, creature5, creature6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An Aura you control attaching to a creature you control creates a Human Soldier")
    void alliedAuraAttachmentCreatesSoldier() {
        Permanent siona = addCreatureReady(player1, new SionaCaptainOfThePyleas());

        enchantWithHolyStrength(player1, siona);

        List<Permanent> tokens = findPermanents(player1, "Human Soldier");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("An opponent's Aura attaching to Siona does not create a token")
    void opponentsAuraDoesNotCreateSoldier() {
        Permanent siona = addCreatureReady(player1, new SionaCaptainOfThePyleas());

        enchantWithHolyStrength(player2, siona);

        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Human Soldier")).isEmpty();
    }

    @Test
    @DisplayName("An Aura you control attaching to an opponent's creature does not create a token")
    void auraOnOpponentsCreatureDoesNotCreateSoldier() {
        addCreatureReady(player1, new SionaCaptainOfThePyleas());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        enchantWithHolyStrength(player1, bears);

        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    private void enchantWithHolyStrength(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new HolyStrength()));
        harness.addMana(controller, ManaColor.WHITE, 1);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
