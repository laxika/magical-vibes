package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.k.KavuScout;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Zap.class, KavuScout.class, ChandraNalaar.class})
class ZapTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature and draws a card")
    void damagesCreatureAndDrawsCard() {
        harness.addToBattlefield(player2, new KavuScout());
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new KavuScout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Kavu Scout"));

        assertThat(findPermanent(player2, "Kavu Scout").getMarkedDamage()).isEqualTo(1);
        harness.assertInHand(player1, "Kavu Scout");
    }

    @Test
    @DisplayName("Deals 1 damage to a player and draws a card")
    void damagesPlayerAndDrawsCard() {
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new KavuScout()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInHand(player1, "Kavu Scout");
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Deals 1 damage to a planeswalker and draws a card")
    void damagesPlaneswalkerAndDrawsCard() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new KavuScout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertInHand(player1, "Kavu Scout");
    }

    @Test
    @DisplayName("Draws a card even when the damage is lethal to the creature")
    void drawsAfterLethalCreatureDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KavuScout());
        creature.setMarkedDamage(1);
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new KavuScout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Kavu Scout");
        harness.assertInGraveyard(player2, "Kavu Scout");
        harness.assertInHand(player1, "Kavu Scout");
        harness.assertInGraveyard(player1, "Zap");
    }

    @Test
    @DisplayName("Does not draw when the sole target leaves before resolution")
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KavuScout());
        creature.setMarkedDamage(1);
        harness.setHand(player1, List.of(new Zap(), new Zap()));
        harness.setLibrary(player1, List.of(new KavuScout(), new KavuScout()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kavu Scout");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target its controller, who still draws the card")
    void damagesControllerAndDrawsCard() {
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new KavuScout()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInHand(player1, "Kavu Scout");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
