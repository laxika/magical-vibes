package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AwakenTheAncient.class, Mountain.class, Forest.class})
class AwakenTheAncientTest extends BaseCardTest {

    private Permanent enchant(Permanent land) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AwakenTheAncient());
        aura.setAttachedTo(land.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted Mountain is a 7/7 red Giant creature with haste")
    void enchantedMountainBecomesCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        enchant(mountain);

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.HASTE)).isTrue();

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, mountain);
        assertThat(bonus.animatedCreature()).isTrue();
        assertThat(bonus.grantedColors()).contains(CardColor.RED);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GIANT);
        assertThat(bonus.grantedCardTypes()).contains(CardType.CREATURE);
    }

    @Test
    @DisplayName("Enchanted Mountain is still a land and taps for red mana")
    void enchantedMountainStillTapsForMana() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        enchant(mountain);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
    }

    @Test
    @DisplayName("Mountain reverts when the Aura leaves the battlefield")
    void mountainRevertsWhenAuraLeaves() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent aura = enchant(mountain);

        assertThat(gqs.isCreature(gd, mountain)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, mountain)).isFalse();
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot cast Awaken the Ancient targeting a non-Mountain land")
    void cannotTargetNonMountain() {
        harness.addToBattlefield(player1, new Mountain()); // valid target so the spell is playable
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AwakenTheAncient()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Mountain");
    }

    @Test
    @DisplayName("Can enchant an opponent's Mountain without taking control of it")
    void resolvesOnOpponentsMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new AwakenTheAncient()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0, mountain.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Awaken the Ancient");
        assertThat(aura.getAttachedTo()).isEqualTo(mountain.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mountain);
        assertThat(gqs.getEffectiveCardTypes(gd, mountain)).contains(CardType.LAND, CardType.CREATURE);
        assertThat(gqs.getEffectiveColors(gd, mountain)).containsExactly(CardColor.RED);
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Aura does not resolve when its Mountain leaves the battlefield")
    void targetGoneBeforeResolution() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new AwakenTheAncient()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castEnchantment(player1, 0, mountain.getId());

        gd.playerBattlefields.get(player1.getId()).remove(mountain);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Awaken the Ancient");
        harness.assertInGraveyard(player1, "Awaken the Ancient");
        assertThat(gd.stack).isEmpty();
    }
}
