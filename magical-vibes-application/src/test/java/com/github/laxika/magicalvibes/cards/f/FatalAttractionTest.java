package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.t.TolariaWest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatalAttraction.class, AncientBrontodon.class, TolariaWest.class})
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
    @DisplayName("Fatal Attraction's upkeep trigger still deals damage after the Aura leaves")
    void upkeepTriggerDealsDamageAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new AncientBrontodon());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FatalAttraction());
        aura.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Fatal Attraction cannot enchant a noncreature permanent")
    void cannotEnchantNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TolariaWest());
        harness.setHand(player1, List.of(new FatalAttraction()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
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
