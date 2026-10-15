package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LukkaCoppercoatOutcast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChanneledForce.class, GrizzlyBears.class, LukkaCoppercoatOutcast.class})
class ChanneledForceTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 makes the target player draw two and deals two damage to the target creature")
    void drawsAndDealsXDamage() {
        harness.setHand(player2, List.of());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(
                new ChanneledForce(), new GrizzlyBears(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        harness.castInstantForXWithDiscards(player1, 0, 2,
                List.of(player2.getId(), bears.getId()), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ChanneledForce);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The creature or planeswalker target may be omitted")
    void mayOmitDamageTarget() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, new ArrayList<>(List.of(
                new ChanneledForce(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.castInstantForXWithDiscards(player1, 0, 1,
                List.of(player2.getId()), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting is rejected when X cards cannot be discarded")
    void rejectsInsufficientDiscardCost() {
        harness.setHand(player1, new ArrayList<>(List.of(new ChanneledForce(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(player1, 0, 2,
                List.of(player2.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("X=0 needs no discards, draws no cards, and deals no damage")
    void zeroDiscards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChanneledForce()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForXWithDiscards(player1, 0, 0,
                List.of(player1.getId(), bears.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Channeled Force");
    }

    @Test
    @DisplayName("The caster may draw while damage removes loyalty from a planeswalker")
    void drawsForCasterAndDamagesPlaneswalker() {
        Permanent lukka = harness.enterBattlefieldAndReturn(player2, new LukkaCoppercoatOutcast());
        harness.setHand(player1, List.of(new ChanneledForce(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForXWithDiscards(player1, 0, 2,
                List.of(player1.getId(), lukka.getId()), List.of(1, 2));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Lukka, Coppercoat Outcast");
    }

    @Test
    @DisplayName("An illegal damage target does not stop the legal player target drawing")
    void drawsWhenDamageTargetLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ChanneledForce(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForXWithDiscards(player1, 0, 1,
                List.of(player2.getId(), bears.getId()), List.of(1));
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.setGraveyard(player2, List.of(bears.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Channeled Force");
    }
}
