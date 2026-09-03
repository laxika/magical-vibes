package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatalAttraction.class, AncientBrontodon.class, FountainOfYouth.class})
class FatalAttractionTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Fatal Attraction deals 2 damage to its enchanted creature")
    void enteringDealsDamageToEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new AncientBrontodon());
        castFatalAttraction(creature);

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fatal Attraction deals 4 damage during its controller's upkeep")
    void controllerUpkeepDealsDamageToEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new AncientBrontodon());
        castFatalAttraction(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(6);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Fatal Attraction cannot enchant a noncreature permanent")
    void cannotEnchantNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new FatalAttraction()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castFatalAttraction(Permanent creature) {
        harness.setHand(player1, List.of(new FatalAttraction()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
