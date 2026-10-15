package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({Arachnoform.class, GrizzlyBears.class, Mountain.class})
class ArachnoformTest extends BaseCardTest {

    private Permanent enchant(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Arachnoform());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2, reach, and all creature types")
    void enchantedCreatureGetsBoostReachAndAllCreatureTypes() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.SPIDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("Arachnoform's effects end when it leaves the battlefield")
    void effectsEndWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = enchant(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.CHANGELING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ELF)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isTrue();
    }

    @Test
    @DisplayName("Arachnoform can enchant only a creature")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Arachnoform()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Arachnoform resolves attached to an opponent's creature and benefits only that creature")
    void resolvesOnOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Arachnoform()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Arachnoform").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.ELF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.REACH)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.ELF)).isFalse();
    }

    @Test
    @DisplayName("Arachnoform does not enter when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Arachnoform()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arachnoform");
        harness.assertInGraveyard(player1, "Arachnoform");
    }
}
