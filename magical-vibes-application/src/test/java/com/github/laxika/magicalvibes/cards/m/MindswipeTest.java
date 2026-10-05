package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mindswipe.class, AlpineGrizzly.class})
class MindswipeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and deals X damage to its controller")
    void countersAndDamagesForX() {
        AlpineGrizzly bears = new AlpineGrizzly();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new Mindswipe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Still deals X damage when the target spell's controller pays")
    void damagesEvenWhenControllerPays() {
        AlpineGrizzly bears = new AlpineGrizzly();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Mindswipe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpine Grizzly");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Damage is dealt only after the controller declines the counter payment")
    void paymentDecisionPrecedesDamage() {
        AlpineGrizzly bears = new AlpineGrizzly();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Mindswipe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertLife(player1, 20);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("X zero allows a free payment and deals no damage")
    void zeroXCanBePaidWithoutMana() {
        AlpineGrizzly bears = new AlpineGrizzly();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new Mindswipe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpine Grizzly");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can counter an instant and damages its controller rather than the caster")
    void countersAnotherMindswipe() {
        AlpineGrizzly bears = new AlpineGrizzly();
        Mindswipe opposingMindswipe = new Mindswipe();
        harness.setHand(player1, List.of(bears, new Mindswipe()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(opposingMindswipe));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 1, opposingMindswipe.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mindswipe");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Alpine Grizzly");
    }

    @Test
    @DisplayName("An illegal spell target prevents Mindswipe from dealing damage")
    void removedTargetPreventsDamage() {
        AlpineGrizzly bears = new AlpineGrizzly();
        harness.setHand(player1, List.of(bears, new Mindswipe()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Mindswipe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.assertLife(player1, 19);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Mindswipe");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var bears = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        harness.setHand(player2, List.of(new Mindswipe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 2, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
