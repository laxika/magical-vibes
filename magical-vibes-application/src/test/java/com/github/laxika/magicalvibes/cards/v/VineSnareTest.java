package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VineSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power 4 or less are prevented from dealing combat damage")
    void preventsSmallCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        Permanent elemental = findPermanent(player2, "Air Elemental");

        castVineSnare();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, elemental, true)).isTrue();
        harness.assertInGraveyard(player1, "Vine Snare");
    }

    @Test
    @DisplayName("Noncombat damage from small creatures is unaffected")
    void doesNotPreventNoncombatDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        castVineSnare();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, false)).isFalse();
    }

    @Test
    @DisplayName("Creatures with power 5 or greater still deal combat damage")
    void exemptsBigCreatures() {
        harness.addToBattlefield(player2, new AvatarOfMight());
        Permanent avatar = findPermanent(player2, "Avatar of Might");

        castVineSnare();

        assertThat(gqs.isPreventedFromDealingDamage(gd, avatar, true)).isFalse();
    }

    private void castVineSnare() {
        harness.setHand(player1, List.of(new VineSnare()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
