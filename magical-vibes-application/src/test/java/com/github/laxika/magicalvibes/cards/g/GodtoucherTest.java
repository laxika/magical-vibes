package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GodtoucherTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage dealt to the targeted power-5+ creature this turn")
    void preventsDamage() {
        addCreatureReady(player1, new Godtoucher());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        // Shock the protected 8/8 — all damage should be prevented, so no damage is marked.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, avatar.getId());
        harness.passBothPriorities();

        assertThat(avatar.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 5")
    void rejectsLowPowerTarget() {
        addCreatureReady(player1, new Godtoucher());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID smallCreature = harness.getPermanentId(player2, "Grizzly Bears");

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, smallCreature))
                .isInstanceOf(IllegalStateException.class);
    }
}
