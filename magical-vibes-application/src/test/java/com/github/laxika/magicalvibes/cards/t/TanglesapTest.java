package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TanglesapTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from creatures without trample")
    void preventsCombatDamageFromCreaturesWithoutTrample() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        castTanglesap();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
    }

    @Test
    @DisplayName("Does not prevent combat damage from creatures with trample")
    void allowsCombatDamageFromCreaturesWithTrample() {
        harness.addToBattlefield(player2, new AvatarOfMight());
        Permanent avatar = findPermanent(player2, "Avatar of Might");

        castTanglesap();

        assertThat(gqs.isPreventedFromDealingDamage(gd, avatar, true)).isFalse();
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from creatures without trample")
    void doesNotPreventNoncombatDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        castTanglesap();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, false)).isFalse();
    }

    private void castTanglesap() {
        harness.setHand(player1, List.of(new Tanglesap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
