package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UndergrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Unkicked, prevents all combat damage this turn")
    void unkickedPreventsAllCombatDamage() {
        harness.setHand(player1, List.of(new Undergrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(gd.combatDamageExemptPredicate).isNull();
        harness.assertInGraveyard(player1, "Undergrowth");
    }

    @Test
    @DisplayName("Kicked, red creatures still deal combat damage")
    void kickedExemptsRedCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent giant = findPermanent(player1, "Hill Giant");

        harness.setHand(player1, List.of(new Undergrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.combatDamageExemptPredicate).isNotNull();
        assertThat(gqs.isPreventedFromDealingDamage(gd, giant, true)).isFalse();
    }

    @Test
    @DisplayName("Kicked, nonred creatures are still prevented from dealing combat damage")
    void kickedStillPreventsNonredCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Undergrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, false)).isFalse();
    }
}
