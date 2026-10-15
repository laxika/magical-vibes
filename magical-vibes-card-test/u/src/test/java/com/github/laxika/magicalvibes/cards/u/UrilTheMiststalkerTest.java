package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Uril, the Miststalker")
@CardUsed({UrilTheMiststalker.class, HolyStrength.class, LeoninScimitar.class, ProdigalPyromancer.class})
class UrilTheMiststalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Without any Aura attached, is 5/5")
    void withoutAurasIs5x5() {
        Permanent uril = addCreatureReady(player1, new UrilTheMiststalker());

        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(5);
    }

    @Test
    @DisplayName("With one Aura attached, gets +2/+2 plus the Aura's stats")
    void withOneAura() {
        Permanent uril = addCreatureReady(player1, new UrilTheMiststalker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(uril.getId());

        // Base 5/5 + 2/2 from Uril (one Aura) + 1/2 from Holy Strength = 8/9
        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(9);
    }

    @Test
    @DisplayName("With two Auras attached, gets +4/+4")
    void withTwoAuras() {
        Permanent uril = addCreatureReady(player1, new UrilTheMiststalker());
        Permanent aura1 = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        Permanent aura2 = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura1.setAttachedTo(uril.getId());
        aura2.setAttachedTo(uril.getId());

        // Base 5/5 + 4/4 from Uril (two Auras) + 2/4 from two Holy Strength = 11/13
        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(13);
    }

    @Test
    @DisplayName("Attached Equipment does not count toward Uril's bonus")
    void equipmentDoesNotCount() {
        Permanent uril = addCreatureReady(player1, new UrilTheMiststalker());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(uril.getId());

        // Base 5/5 + 0 from Uril (Equipment is not an Aura) + 1/1 from Leonin Scimitar = 6/6
        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(6);
    }

    @Test
    @DisplayName("Auras on other creatures do not count")
    void aurasOnOtherCreaturesDoNotCount() {
        Permanent uril = addCreatureReady(player1, new UrilTheMiststalker());
        Permanent other = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(other.getId());

        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(5);
    }

    @Test
    void controllerCanEnchantUril() {
        Permanent uril = addCreatureReady(player1, new UrilTheMiststalker());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, uril.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(uril.getId());
        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(9);
    }

    @Test
    void opponentCannotEnchantUril() {
        Permanent uril = addCreatureReady(player2, new UrilTheMiststalker());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, uril.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCannotTargetUrilWithActivatedAbility() {
        Permanent uril = addCreatureReady(player2, new UrilTheMiststalker());
        addCreatureReady(player1, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, uril.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsAttachedAuraCounts() {
        Permanent uril = addCreatureReady(player1, new UrilTheMiststalker());
        // An Aura can become attached without targeting, even through hexproof.
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(uril.getId());

        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(9);
    }

    @Test
    void bonusUpdatesWhenAuraLeavesBattlefield() {
        Permanent uril = addCreatureReady(player1, new UrilTheMiststalker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(uril.getId());
        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(9);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(5);
    }
}
