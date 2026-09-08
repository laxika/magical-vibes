package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherCharge.class, AxebaneBeast.class, ElspethKnightErrant.class, GrizzlyBears.class})
class AetherChargeTest extends BaseCardTest {

    @Test
    @DisplayName("A Beast entering under your control may deal 4 damage to an opponent")
    void beastMayDealDamageToOpponent() {
        addCharge();
        harness.setHand(player1, List.of(new AxebaneBeast()));
        addBeastMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The Beast's damage may be dealt to an opponent's planeswalker")
    void beastMayDealDamageToPlaneswalker() {
        addCharge();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new AxebaneBeast()));
        addBeastMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the Beast trigger deals no damage")
    void decliningTriggerDealsNoDamage() {
        addCharge();
        harness.setHand(player1, List.of(new AxebaneBeast()));
        addBeastMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A non-Beast creature does not trigger Aether Charge")
    void nonBeastDoesNotTrigger() {
        addCharge();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    private void addCharge() {
        harness.addToBattlefield(player1, new AetherCharge());
    }

    private void addBeastMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
