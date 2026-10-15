package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({MarshalsAnthem.class, GrizzlyBears.class, Shock.class})
class MarshalsAnthemTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, it returns no creatures")
    void returnsNoCreaturesWithoutMultikicker() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MarshalsAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Marshal's Anthem");
    }

    @Test
    @DisplayName("Returns up to one creature for one multikicker payment")
    void returnsOneCreatureForOneMultikickerPayment() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MarshalsAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantmentWithRepeatedCosts(player1, 0, List.of("{1}{W}"));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(returnedBears).isNotNull();
        assertThat(gqs.getEffectivePower(gd, returnedBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedBears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Returns fewer than the multikicker cap and only offers creature cards")
    void returnsUpToTwoCreatureCards() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock));
        harness.setHand(player1, List.of(new MarshalsAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantmentWithRepeatedCosts(player1, 0, List.of("{1}{W}", "{1}{W}"));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Two multikicker payments return two creatures from only your graveyard")
    void returnsTwoCreaturesFromControllersGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card opponentsCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setHand(player1, List.of(new MarshalsAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantmentWithRepeatedCosts(player1, 0, List.of("{1}{W}", "{1}{W}"));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1)).filteredOn(p -> p.getCard() instanceof GrizzlyBears)
                .hasSize(2).allSatisfy(p -> {
                    assertThat(p.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(3);
                });
        assertThat(gd.playerGraveyards.get(player1)).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A kicked Anthem allows choosing zero creatures")
    void mayChooseZeroCreatures() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MarshalsAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantmentWithRepeatedCosts(player1, 0, List.of("{1}{W}"));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Marshal's Anthem");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({MarshalsAnthem.class, Opalescence.class})
    @DisplayName("Anthem boosts itself when it becomes a creature")
    void boostsItselfWhenAnimated() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new MarshalsAnthem());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, anthem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, anthem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, anthem)).isEqualTo(5);
    }

    @Test
    @DisplayName("Anthem boosts existing and newly entering friendly creatures only")
    void boostsOnlyControllersCreatures() {
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarshalsAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        for (Permanent creature : List.of(friendly, newcomer)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        }
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }
}
