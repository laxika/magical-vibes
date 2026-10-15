package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimalVisitation.class, GrizzlyBears.class, Forest.class})
class PrimalVisitationTest extends BaseCardTest {

    private Permanent enchantedBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PrimalVisitation());
        aura.setAttachedTo(bears.getId());
        return bears;
    }

    @Test
    @DisplayName("Enchanted creature gets +3/+3 and has haste")
    void enchantedCreatureBoostedAndHasHaste() {
        Permanent bears = enchantedBears();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Boost and haste end when the Aura leaves the battlefield")
    void effectsEndWhenAuraLeaves() {
        Permanent bears = enchantedBears();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof PrimalVisitation);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new PrimalVisitation()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent land = findPermanent(player1, "Forest");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting on an opponent's creature grants only that creature the boost and haste")
    void resolvesOnOpponentCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalVisitation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Primal Visitation").getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, host, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Aura goes to the graveyard when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalVisitation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, host.getId());

        gd.playerBattlefields.get(player1.getId()).remove(host);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Primal Visitation");
        harness.assertNotOnBattlefield(player1, "Primal Visitation");
    }

    @Test
    @DisplayName("Haste lets a newly controlled enchanted creature attack")
    void enchantedCreatureCanAttackImmediately() {
        Permanent host = enchantedBears();
        host.setSummoningSick(true);

        declareAttackers(List.of(0));

        assertThat(host.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Two copies stack their boosts and haste lasts until the last copy leaves")
    void multipleCopiesApplyIndependently() {
        Permanent host = enchantedBears();
        Permanent first = findPermanent(player1, "Primal Visitation");
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PrimalVisitation());
        second.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, host, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, host, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.HASTE)).isFalse();
    }
}
