package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TanglespanLookout.class, Pacifism.class, GloriousAnthem.class, GrizzlyBears.class})
class TanglespanLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an Aura you control enters")
    void drawsWhenControlledAuraEnters() {
        harness.addToBattlefield(player1, new TanglespanLookout());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism(), new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger for a non-Aura enchantment")
    void doesNotDrawForNonAuraEnchantment() {
        harness.addToBattlefield(player1, new TanglespanLookout());
        harness.setHand(player1, List.of(new GloriousAnthem(), new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for an Aura controlled by an opponent")
    void doesNotDrawForOpponentsAura() {
        harness.addToBattlefield(player1, new TanglespanLookout());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
