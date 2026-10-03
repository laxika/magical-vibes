package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoilingOracle;
import com.github.laxika.magicalvibes.cards.h.HallowedFountain;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessingOfTheNephilim.class, CoilingOracle.class, MistralCharger.class,
        BronzeBombshell.class, HallowedFountain.class})
class BlessingOfTheNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each of its colors")
    void boostsByColorCount() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoilingOracle());

        castBlessing(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A monocolored enchanted creature gets +1/+1")
    void boostsMonocoloredCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());

        castBlessing(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A colorless enchanted creature gets no bonus")
    void doesNotBoostColorlessCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeBombshell());

        castBlessing(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus is removed when Blessing of the Nephilim leaves the battlefield")
    void bonusStopsWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoilingOracle());

        castBlessing(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        Permanent aura = findPermanent(player1, "Blessing of the Nephilim");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HallowedFountain());
        harness.setHand(player1, List.of(new BlessingOfTheNephilim()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enchant and boost an opponent's creature without boosting other creatures")
    void boostsOpponentsCreatureOnly() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CoilingOracle());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MistralCharger());

        castBlessing(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Blessing of the Nephilim");
    }

    @Test
    @DisplayName("Multiple Blessings each grant their own bonus")
    void multipleBlessingsStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoilingOracle());

        castBlessing(creature);
        castBlessing(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blessing does not resolve when its target leaves the battlefield")
    void doesNotResolveWithMissingTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoilingOracle());
        harness.setHand(player1, List.of(new BlessingOfTheNephilim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blessing of the Nephilim");
        harness.assertInGraveyard(player1, "Blessing of the Nephilim");
    }

    private void castBlessing(Permanent creature) {
        harness.setHand(player1, List.of(new BlessingOfTheNephilim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
