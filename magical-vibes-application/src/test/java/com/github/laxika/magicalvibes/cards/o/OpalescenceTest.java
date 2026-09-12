package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CapashenTemplar;
import com.github.laxika.magicalvibes.cards.i.IlluminatedWings;
import com.github.laxika.magicalvibes.cards.m.MentalDiscipline;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Opalescence.class, Opposition.class, MentalDiscipline.class, IlluminatedWings.class,
        CapashenTemplar.class})
class OpalescenceTest extends BaseCardTest {

    @Test
    @DisplayName("Other non-Aura enchantments on both battlefields become creatures with mana-value P/T")
    void animatesOtherNonAuraEnchantmentsOnBothBattlefields() {
        Permanent opposition = harness.addToBattlefieldAndReturn(player1, new Opposition());
        Permanent mentalDiscipline = harness.addToBattlefieldAndReturn(player2, new MentalDiscipline());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, opposition)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposition)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposition)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, mentalDiscipline)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mentalDiscipline)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mentalDiscipline)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opalescence does not animate itself")
    void doesNotAnimateItself() {
        Permanent opalescence = harness.addToBattlefieldAndReturn(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, opalescence)).isFalse();
    }

    @Test
    @DisplayName("Auras are not animated")
    void doesNotAnimateAuras() {
        Permanent templar = addCreatureReady(player1, new CapashenTemplar());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IlluminatedWings());
        harness.addToBattlefield(player1, new Opalescence());
        aura.setAttachedTo(templar.getId());

        assertThat(gqs.isEnchantment(gd, aura)).isTrue();
        assertThat(gqs.isCreature(gd, aura)).isFalse();
    }

    @Test
    @DisplayName("Animated enchantments retain their enchantment type and abilities")
    void retainsOtherTypesAndAbilities() {
        Permanent opposition = harness.addToBattlefieldAndReturn(player1, new Opposition());
        Permanent costCreature = addCreatureReady(player1, new CapashenTemplar());
        Permanent target = addCreatureReady(player2, new CapashenTemplar());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isEnchantment(gd, opposition)).isTrue();
        assertThat(gqs.isCreature(gd, opposition)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposition)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposition)).isEqualTo(4);

        opposition.tap();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(costCreature.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The animation ends when Opalescence leaves the battlefield")
    void animationEndsWhenSourceLeaves() {
        Permanent opposition = harness.addToBattlefieldAndReturn(player1, new Opposition());
        Permanent opalescence = harness.addToBattlefieldAndReturn(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, opposition)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(opalescence);

        assertThat(gqs.isEnchantment(gd, opposition)).isTrue();
        assertThat(gqs.isCreature(gd, opposition)).isFalse();
    }

    @Test
    @DisplayName("Two Opalescences animate each other")
    void otherOpalescenceIsAnimated() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Opalescence());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, first)).isTrue();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, second)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }
}
