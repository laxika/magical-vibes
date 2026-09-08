package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RavenWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0, flying, and Bird in addition to its other types")
    void equippedCreatureGetsBonuses() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wings = addWingsReady(player1);
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
        addWingsReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes()).doesNotContain(CardSubtype.BIRD);
    }

    @Test
    @DisplayName("Equip ability attaches Raven Wings to a creature you control")
    void equipAbilityAttachesToCreature() {
        Permanent wings = addWingsReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addWingsReady(Player player) {
        Permanent permanent = new Permanent(new RavenWings());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
