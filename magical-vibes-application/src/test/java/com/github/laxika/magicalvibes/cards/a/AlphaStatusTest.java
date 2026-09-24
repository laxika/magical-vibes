package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlphaStatus.class, AvenFarseer.class, AvenLiberator.class, GoblinBrigand.class,
        TempleOfTheFalseGod.class})
class AlphaStatusTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 for each other creature sharing a type with it")
    void boostsEnchantedCreatureForEachOtherSharingCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new AvenFarseer());
        harness.addToBattlefield(player1, new AvenFarseer());
        harness.addToBattlefield(player2, new AvenLiberator());
        harness.addToBattlefield(player1, new GoblinBrigand());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaStatus());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
    }

    @Test
    @DisplayName("The bonus updates as sharing creatures enter and leave")
    void bonusUpdatesDynamically() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new AvenFarseer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaStatus());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);

        Permanent other = harness.addToBattlefieldAndReturn(player2, new AvenLiberator());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);

        gd.playerBattlefields.get(player2.getId()).remove(other);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unattached Alpha Status and unrelated creatures provide no bonus")
    void unattachedOrUnrelatedProvidesNoBonus() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaStatus());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new AvenFarseer());
        harness.addToBattlefield(player1, new GoblinBrigand());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);

        aura.setAttachedTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
    }

    @Test
    @CardUsed(WoodlandChangeling.class)
    @DisplayName("A changeling counts as sharing a creature type")
    void changelingCountsAsSharingCreatureType() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new AvenFarseer());
        harness.addToBattlefield(player2, new WoodlandChangeling());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaStatus());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
    }

    @Test
    @DisplayName("Alpha Status cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfTheFalseGod());
        harness.setHand(player1, List.of(new AlphaStatus()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The bonus ends when Alpha Status leaves the battlefield")
    void bonusEndsWhenAuraLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new AvenFarseer());
        harness.addToBattlefield(player1, new AvenLiberator());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaStatus());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
    }
}
