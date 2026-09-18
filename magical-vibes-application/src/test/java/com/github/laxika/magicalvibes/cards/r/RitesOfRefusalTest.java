package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RitesOfRefusal.class, WoodlandDruid.class})
class RitesOfRefusalTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell when its controller cannot pay three mana per discarded card")
    void countersWhenControllerCannotPayScaledCost() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid, new RitesOfRefusal(), new RitesOfRefusal()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new RitesOfRefusal(), new RitesOfRefusal(), new RitesOfRefusal()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, druid.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player2, 2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Woodland Druid");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller can pay three mana per discarded card")
    void controllerCanPayScaledCost() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid, new RitesOfRefusal(), new RitesOfRefusal()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.setHand(player2, List.of(new RitesOfRefusal(), new RitesOfRefusal(), new RitesOfRefusal()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, druid.getId());
        harness.passBothPriorities();

        harness.handleXValueChosen(player2, 2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Woodland Druid");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The controller may decline to pay and the spell is countered")
    void controllerMayDeclineScaledPayment() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new RitesOfRefusal(), new WoodlandDruid()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, druid.getId());
        harness.passBothPriorities();

        harness.handleXValueChosen(player2, 1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Woodland Druid");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Discarding zero cards makes the payment zero")
    void canDiscardZeroCards() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new RitesOfRefusal()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, druid.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Woodland Druid");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller can choose zero while cards remain in hand")
    void canChooseZeroWithCardsInHand() {
        WoodlandDruid druid = new WoodlandDruid();
        WoodlandDruid cardToKeep = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new RitesOfRefusal(), cardToKeep));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, druid.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Woodland Druid");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cardToKeep);
    }
}
