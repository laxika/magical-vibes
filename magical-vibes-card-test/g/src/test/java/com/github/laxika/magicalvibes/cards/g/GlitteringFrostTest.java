package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlitteringFrost.class, Forest.class})
class GlitteringFrostTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes snow")
    void enchantedLandBecomesSnow() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlitteringFrost());
        aura.setAttachedTo(forest.getId());

        assertThat(gqs.hasEffectiveSupertype(gd, forest, CardSupertype.SNOW)).isTrue();
    }

    @Test
    @DisplayName("Tapping enchanted land adds one mana of a chosen color")
    void tappingEnchantedLandAddsAnyColorMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlitteringFrost());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Snow effect ends when Glittering Frost leaves the battlefield")
    void snowEffectEndsWhenAuraLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlitteringFrost());
        aura.setAttachedTo(forest.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasEffectiveSupertype(gd, forest, CardSupertype.SNOW)).isFalse();
    }

    @Test
    @DisplayName("Both the land's mana and Glittering Frost's additional mana can pay snow costs")
    void bothManaAreFromSnowSources() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlitteringFrost());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller of an opponent's enchanted land chooses and receives the additional mana")
    void opponentReceivesAdditionalMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlitteringFrost());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player2, 0);
        harness.handleListChoice(player2, "WHITE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.hasEffectiveSupertype(gd, forest, CardSupertype.SNOW)).isTrue();
    }

    @Test
    @DisplayName("An unrelated land neither becomes snow nor produces additional mana")
    void unrelatedLandIsUnaffected() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlitteringFrost());
        aura.setAttachedTo(enchanted.getId());

        harness.tapPermanent(player1, 1);

        assertThat(gqs.hasEffectiveSupertype(gd, other, CardSupertype.SNOW)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Casting Glittering Frost attaches it to the targeted land")
    void castingAttachesToTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GlitteringFrost()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isInstanceOf(GlitteringFrost.class);
                    assertThat(permanent.getAttachedTo()).isEqualTo(forest.getId());
                });
        assertThat(gqs.hasEffectiveSupertype(gd, forest, CardSupertype.SNOW)).isTrue();
    }
}
