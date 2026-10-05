package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PygmyAllosaurus;
import com.github.laxika.magicalvibes.cards.t.ThunderingSpineback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IxallisLorekeeper.class, PygmyAllosaurus.class, GrizzlyBears.class,
        ThunderingSpineback.class, FountainOfYouth.class})
class IxallisLorekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Mana can be spent to cast a Dinosaur spell")
    void manaCanBeSpentToCastDinosaurSpell() {
        addRestrictedMana();
        harness.setHand(player1, List.of(new PygmyAllosaurus()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pygmy Allosaurus")).isNotNull();
    }

    @Test
    @DisplayName("Mana cannot be spent to cast a non-Dinosaur spell")
    void manaCannotBeSpentToCastNonDinosaurSpell() {
        addRestrictedMana();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana can be spent to activate an ability of a Dinosaur source")
    void manaCanBeSpentToActivateDinosaurAbility() {
        addRestrictedMana();
        addCreatureReady(player1, new ThunderingSpineback());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
    }

    @Test
    @DisplayName("Mana cannot be spent to activate an ability of a non-Dinosaur source")
    void manaCannotBeSpentToActivateNonDinosaurAbility() {
        addRestrictedMana();
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Any chosen color can pay a Dinosaur spell's generic cost")
    void anyColorCanPayDinosaurGenericCost(ManaColor color) {
        addCreatureReady(player1, new IxallisLorekeeper());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());
        harness.setHand(player1, List.of(new PygmyAllosaurus()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pygmy Allosaurus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restricted mana cannot pay even a non-Dinosaur spell's generic cost")
    void restrictedManaCannotPayNonDinosaurGenericCost() {
        addCreatureReady(player1, new IxallisLorekeeper());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana can pay the generic part of a Dinosaur ability")
    void restrictedManaCanPayDinosaurAbilityGenericCost() {
        addCreatureReady(player1, new IxallisLorekeeper());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        addCreatureReady(player1, new ThunderingSpineback());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
    }

    @Test
    @DisplayName("The mana ability resolves immediately and taps its source")
    void manaAbilityResolvesImmediatelyAndCannotBeActivatedTwice() {
        addRestrictedMana();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Ixalli's Lorekeeper").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Lorekeeper cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new IxallisLorekeeper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One activation produces only one mana")
    void oneActivationDoesNotProduceTwoMana() {
        addRestrictedMana();
        harness.setHand(player1, List.of(new PygmyAllosaurus()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addRestrictedMana() {
        addCreatureReady(player1, new IxallisLorekeeper());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
    }
}
