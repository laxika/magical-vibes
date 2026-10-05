package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.o.ObeliskOfEsper;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningTalons.class, CylianElf.class, ObeliskOfEsper.class})
class LightningTalonsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Lightning Talons attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        harness.setHand(player1, List.of(new LightningTalons()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lightning Talons")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +3/+0 and first strike")
    void enchantedCreatureGetsBoostAndFirstStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LightningTalons());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature returns to base stats and loses first strike when Lightning Talons is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LightningTalons());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Lightning Talons")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new CylianElf());
        harness.addToBattlefield(player1, new ObeliskOfEsper());
        harness.setHand(player1, List.of(new LightningTalons()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent artifact = findPermanent(player1, "Obelisk of Esper");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Lightning Talons can enchant an opponent's creature without boosting other creatures")
    void enchantsOpponentsCreatureOnly() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new LightningTalons()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lightning Talons").getAttachedTo()).isEqualTo(enchanted.getId());
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Two Lightning Talons stack their power bonuses and either one still grants first strike")
    void multipleCopiesStackAndRemainingCopyStillApplies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LightningTalons());
        first.setAttachedTo(creature.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LightningTalons());
        second.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }
}
