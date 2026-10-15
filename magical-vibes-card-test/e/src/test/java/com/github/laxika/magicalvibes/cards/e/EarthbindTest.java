package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Earthbind.class, SerraAngel.class, GrizzlyBears.class, Jump.class, Disenchant.class})
class EarthbindTest extends BaseCardTest {

    @Test
    @DisplayName("Earthbind deals 2 damage and removes flying from a flying creature")
    void damagesAndRemovesFlying() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.setHand(player1, List.of(new Earthbind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, angel.getId());
        resolveAllTriggers();

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Earthbind does nothing when the enchanted creature has no flying")
    void doesNothingToNonFlyingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Earthbind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A later flying grant applies after Earthbind's removal")
    void laterFlyingGrantApplies() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.setHand(player1, List.of(new Earthbind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, angel.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, angel.getId());

        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Earthbind's flying removal ends when Earthbind leaves the battlefield")
    void removalEndsWhenEarthbindLeavesBattlefield() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.setHand(player1, List.of(new Earthbind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, angel.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();

        UUID earthbindId = harness.getPermanentId(player1, "Earthbind");
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, earthbindId);

        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Earthbind still deals damage if it leaves before its trigger resolves")
    void triggerDealsDamageAfterEarthbindLeavesBattlefield() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.setHand(player1, List.of(new Earthbind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, angel.getId());
        harness.passBothPriorities();

        UUID earthbindId = harness.getPermanentId(player1, "Earthbind");
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, earthbindId);
        resolveAllTriggers();

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A creature without flying on entry can gain flying without triggering Earthbind")
    void nonFlyingCreatureCanGainFlyingLater() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Earthbind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();

        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Earthbind removes flying granted in response to its entry trigger")
    void removesFlyingGrantedBeforeTriggerResolves() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.setHand(player1, List.of(new Earthbind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, angel.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, angel.getId());
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        resolveAllTriggers();

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }
}
