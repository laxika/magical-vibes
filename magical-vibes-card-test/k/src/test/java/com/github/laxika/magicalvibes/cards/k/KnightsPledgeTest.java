package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightsPledge.class, GreenwoodSentinel.class, Manalith.class})
class KnightsPledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Knight's Pledge attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new KnightsPledge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Knight's Pledge")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent pledge = harness.addToBattlefieldAndReturn(player1, new KnightsPledge());
        pledge.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost goes away when the Aura leaves the battlefield")
    void boostEndsWhenAuraLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent pledge = harness.addToBattlefieldAndReturn(player1, new KnightsPledge());
        pledge.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(pledge);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Manalith());
        harness.setHand(player1, List.of(new KnightsPledge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enchant an opponent's creature without boosting other creatures")
    void canEnchantOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new KnightsPledge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Knight's Pledge").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple copies each grant their boost")
    void multipleCopiesStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new KnightsPledge(), new KnightsPledge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Knight's Pledge")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not enter when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new KnightsPledge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerHands.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Knight's Pledge");
        harness.assertInGraveyard(player1, "Knight's Pledge");
    }
}
