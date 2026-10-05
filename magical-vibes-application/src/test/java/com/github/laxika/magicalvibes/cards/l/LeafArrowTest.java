package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DawnglareInvoker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeafArrow.class, AirElemental.class, GrizzlyBears.class, DawnglareInvoker.class})
class LeafArrowTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target creature with flying")
    void dealsThreeDamageToFlyingCreature() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new LeafArrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, flyer.getId());

        assertThat(flyer.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LeafArrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, groundCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new LeafArrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lethal damage destroys a flying creature")
    void destroysFlyingCreatureWithLethalDamage() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new DawnglareInvoker());
        harness.setHand(player1, List.of(new LeafArrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, flyer.getId());

        harness.assertNotOnBattlefield(player2, "Dawnglare Invoker");
        harness.assertInGraveyard(player2, "Dawnglare Invoker");
        harness.assertInGraveyard(player1, "Leaf Arrow");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target a flying creature controlled by its caster")
    void canTargetOwnFlyingCreature() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new LeafArrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, flyer.getId());

        assertThat(flyer.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not resolve when its target leaves the battlefield in response")
    void doesNotResolveWhenTargetLeavesBattlefield() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new DawnglareInvoker());
        harness.setHand(player1, List.of(new LeafArrow()));
        harness.setHand(player2, List.of(new LeafArrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, flyer.getId());
        harness.castAndResolveInstant(player2, 0, flyer.getId());
        harness.assertInGraveyard(player2, "Dawnglare Invoker");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Leaf Arrow");
        harness.assertInGraveyard(player2, "Leaf Arrow");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
