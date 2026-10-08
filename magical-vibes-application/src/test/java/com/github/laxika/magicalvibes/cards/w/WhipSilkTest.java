package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
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

@CardUsed({WhipSilk.class, KavuTitan.class, Forest.class})
class WhipSilkTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Whip Silk attaches it and grants reach")
    void resolvingAttachesAndGrantsReach() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuTitan());

        harness.setHand(player1, List.of(new WhipSilk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, kavu.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof WhipSilk
                        && p.isAttached()
                        && kavu.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature has reach")
    void enchantedCreatureHasReach() {
        Permanent kavu = attachWhipSilk();

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Whip Silk grants reach only to its enchanted creature")
    void onlyEnchantedCreatureHasReach() {
        Permanent enchanted = attachWhipSilk();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new KavuTitan());

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Creature loses reach when Whip Silk leaves the battlefield")
    void reachLostWhenAuraLeaves() {
        Permanent kavu = attachWhipSilk();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Whip Silk"));

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Activated ability returns Whip Silk to its owner's hand")
    void activatedAbilityReturnsSelfToHand() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuTitan());

        harness.setHand(player1, List.of(new WhipSilk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, kavu.getId());
        harness.passBothPriorities();

        int auraIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Whip Silk"));

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Whip Silk");
        harness.assertInHand(player1, "Whip Silk");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new WhipSilk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Whip Silk can enchant an opponent's creature and return without bouncing it")
    void enchantsOpponentCreatureAndReturnsOnlyAura() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuTitan());
        harness.setHand(player1, List.of(new WhipSilk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, kavu.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Whip Silk");
        assertThat(aura.getAttachedTo()).isEqualTo(kavu.getId());
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.REACH)).isTrue();
        aura.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        harness.assertOnBattlefield(player1, "Whip Silk");
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.REACH)).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Whip Silk");
        harness.assertInHand(player1, "Whip Silk");
        harness.assertOnBattlefield(player2, "Kavu Titan");
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.REACH)).isFalse();
    }

    private Permanent attachWhipSilk() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuTitan());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WhipSilk());
        aura.setAttachedTo(kavu.getId());

        return kavu;
    }
}
