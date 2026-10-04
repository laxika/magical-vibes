package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ghostfire.class, GrizzlyBears.class, PaladinEnVec.class, ChandraNalaar.class})
class GhostfireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a player")
    void dealsThreeDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage to a creature")
    void dealsThreeDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("As a colorless spell, Ghostfire can damage a creature with protection from red")
    void colorlessSpellCanDamageCreatureWithProtectionFromRed() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Paladin en-Vec"));

        harness.assertNotOnBattlefield(player2, "Paladin en-Vec");
    }

    @Test
    @DisplayName("Deals 3 damage to a planeswalker")
    void dealsThreeDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can deal 3 damage to its controller")
    void canDamageItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Ghostfire");
    }

    @Test
    @DisplayName("A planeswalker with 3 loyalty dies from Ghostfire's damage")
    void killsPlaneswalkerWithThreeLoyalty() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertInGraveyard(player2, "Chandra Nalaar");
    }

    @Test
    @DisplayName("Being colorless does not remove the red mana requirement")
    void cannotBeCastWithOnlyColorlessMana() {
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }
}
