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
        Permanent angel = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).add(angel);

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
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

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
        Permanent angel = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).add(angel);

        harness.setHand(player1, List.of(new Earthbind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, angel.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Earthbind's flying removal ends when Earthbind leaves the battlefield")
    void removalEndsWhenEarthbindLeavesBattlefield() {
        Permanent angel = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).add(angel);

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
    @DisplayName("Earthbind's trigger does nothing if Earthbind leaves before it resolves")
    void triggerDoesNothingAfterEarthbindLeavesBattlefield() {
        Permanent angel = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).add(angel);

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

        assertThat(angel.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }
}
