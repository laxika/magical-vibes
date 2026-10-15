package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenWings.class, GrizzlyBears.class})
class RavenWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0, flying, and Bird in addition to its other types")
    void equippedCreatureGetsBonuses() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new RavenWings());
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes()).contains(CardSubtype.BIRD);
    }

    @Test
    @DisplayName("Raven Wings does not affect an unequipped creature")
    void unequippedCreatureIsUnaffected() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new RavenWings());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes()).doesNotContain(CardSubtype.BIRD);
    }

    @Test
    @DisplayName("Equip ability attaches Raven Wings to a creature you control")
    void equipAbilityAttachesToCreature() {
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new RavenWings());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Reequipping moves every bonus and preserves the creature's original subtype")
    void reequippingMovesBonuses() {
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new RavenWings());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.BIRD)).isTrue();

        harness.activateAbility(player1, 0, null, second.getId());
        assertThat(wings.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.BIRD)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.BIRD)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.BEAR)).isTrue();
    }
}
