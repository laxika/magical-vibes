package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamiyosCompleation.class, AirElemental.class, GrizzlyBears.class, JaceBeleren.class,
        LeoninScimitar.class})
class TamiyosCompleationTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Tamiyo's Compleation to be cast during an opponent's turn")
    void canCastAtInstantSpeed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Entering Tamiyo's Compleation taps an enchanted permanent")
    void enteringAuraTapsEnchantedPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering Tamiyo's Compleation unattaches an enchanted Equipment")
    void enteringAuraUnattachesEnchantedEquipment() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, equipment.getId());
        resolveAllTriggers();

        assertThat(equipment.isTapped()).isTrue();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Enchanted permanents lose abilities and do not untap")
    void enchantedPermanentLosesAbilitiesAndDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, com.github.laxika.magicalvibes.model.Keyword.FLYING))
                .isFalse();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tamiyo's Compleation can enchant a planeswalker")
    void canEnchantPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();
        harness.castEnchantment(player1, 0, planeswalker.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Tamiyo's Compleation").getAttachedTo())
                .isEqualTo(planeswalker.getId());
    }

    @Test
    @DisplayName("Tamiyo's Compleation cannot enchant an enchantment")
    void cannotEnchantEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new TamiyosCompleation());

        harness.setHand(player1, List.of(new TamiyosCompleation()));
        addManaForTamiyosCompleation();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or planeswalker");
    }

    private void addManaForTamiyosCompleation() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToNextTurn(com.github.laxika.magicalvibes.model.Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
