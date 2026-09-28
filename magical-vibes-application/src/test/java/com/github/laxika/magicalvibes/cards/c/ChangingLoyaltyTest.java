package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChangingLoyalty.class, GrizzlyBears.class})
class ChangingLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the enchanted creature under the Aura controller's control when it dies")
    void returnsEnchantedCreatureUnderAuraControllersControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card creatureCard = creature.getCard();

        castChangingLoyalty(creature, List.of());
        creature.setMarkedDamage(100);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureCard.getId()));
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Changing Loyalty");
    }

    @Test
    @DisplayName("Replicate creates token copies and allows them to choose new Aura targets")
    void replicateCreatesTokenCopyWithNewTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent copiedTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castChangingLoyalty(originalTarget, List.of("{2}"));

        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copiedTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> auras = findPermanents(player1, "Changing Loyalty");
        assertThat(auras).hasSize(2);
        assertThat(auras.stream().filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(auras).extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(originalTarget.getId(), copiedTarget.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent aura = new Permanent(new ChangingLoyalty());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.setHand(player1, List.of(new ChangingLoyalty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castChangingLoyalty(Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new ChangingLoyalty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1 + replicatePayments.size() * 2);
        harness.castInstantWithRepeatedCosts(player1, 0, target.getId(), replicatePayments);
        if (replicatePayments.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
