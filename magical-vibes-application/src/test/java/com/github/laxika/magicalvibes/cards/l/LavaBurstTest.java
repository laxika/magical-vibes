package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.s.SacredBoon;
import com.github.laxika.magicalvibes.cards.z.ZealousInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavaBurst.class, BalduvianBears.class, SacredBoon.class, CircleOfProtectionRed.class, ZealousInquisitor.class})
class LavaBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new LavaBurst()));
        harness.addMana(player1, ManaColor.RED, 4); // X=3 + {R}
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals X damage to target creature, destroying it")
    void dealsXDamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new LavaBurst()));
        harness.addMana(player1, ManaColor.RED, 3); // X=2 + {R}

        UUID targetId = harness.getPermanentId(player2, "Balduvian Bears");
        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Damage dealt to a creature can't be prevented")
    void creatureDamageCannotBePrevented() {
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new LavaBurst()));
        harness.addMana(player1, ManaColor.RED, 3); // X=2 + {R}
        harness.setHand(player2, List.of(new SacredBoon()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Balduvian Bears");
        harness.castSorcery(player1, 0, 2, targetId);
        harness.passPriority(player1);
        // Player2 shields the Bears for the next 3 damage in response — but this damage
        // can't be prevented, so the Bears still take 2 and die.
        harness.castInstant(player2, 0, 1, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Damage dealt to a player can still be prevented")
    void playerDamageCanBePrevented() {
        Permanent circle = addCreatureReady(player2, new CircleOfProtectionRed());
        LavaBurst lavaBurst = new LavaBurst();
        harness.setHand(player1, List.of(lavaBurst));
        harness.addMana(player1, ManaColor.RED, 4); // X=3 + {R}
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passPriority(player1);
        int circleIndex = gd.playerBattlefields.get(player2.getId()).indexOf(circle);
        harness.activateAbility(player2, circleIndex, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, lavaBurst.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage dealt to a creature can't be redirected")
    void creatureDamageCannotBeRedirected() {
        Permanent inquisitor = addCreatureReady(player1, new ZealousInquisitor());
        Permanent destination = addCreatureReady(player1, new BalduvianBears());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(inquisitor),
                null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LavaBurst()));
        harness.addMana(player1, ManaColor.RED, 2); // X=1 + {R}
        harness.castAndResolveSorcery(player1, 0, 1, inquisitor.getId());

        assertThat(inquisitor.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("X can be zero without dealing damage")
    void zeroXDealsNoDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new LavaBurst()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Lava Burst");
    }

    @Test
    @DisplayName("Unpreventable creature damage does not make later player damage unpreventable")
    void creatureDamageDoesNotDisableLaterPlayerPrevention() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent circle = harness.addToBattlefieldAndReturn(player2, new CircleOfProtectionRed());
        LavaBurst secondBurst = new LavaBurst();
        harness.setHand(player1, List.of(new LavaBurst(), secondBurst));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 1, bears.getId());
        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(circle), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, secondBurst.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }
}
