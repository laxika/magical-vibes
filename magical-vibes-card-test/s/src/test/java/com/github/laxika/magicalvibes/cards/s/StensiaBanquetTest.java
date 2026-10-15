package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.l.LilianaTheLastHope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({StensiaBanquet.class, CaptivatingVampire.class, Shock.class, LilianaTheLastHope.class})
class StensiaBanquetTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to your Vampires and draws a card")
    void dealsDamageAndDrawsCard() {
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Counts only Vampires controlled by the spell's controller")
    void countsOnlyControllerVampires() {
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player2, new CaptivatingVampire());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
        harness.assertInHand(player1, "Shock");
    }

    @Test
    void drawsCardWithNoVampires() {
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Shock");
    }

    @Test
    void countsVampiresAtResolution() {
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new CaptivatingVampire());

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInHand(player1, "Shock");
    }

    @Test
    void canDamageOwnPlaneswalker() {
        harness.addToBattlefield(player1, new LilianaTheLastHope());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new CaptivatingVampire());
        }
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0,
                harness.getPermanentId(player1, "Liliana, the Last Hope"));

        harness.assertInGraveyard(player1, "Liliana, the Last Hope");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Shock");
    }

    @Test
    void doesNotDrawWhenPlaneswalkerTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new LilianaTheLastHope());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Liliana, the Last Hope"));
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Stensia Banquet");
    }

    @Test
    void cannotTargetController() {
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new CaptivatingVampire());
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                harness.castSorcery(player1, 0,
                        harness.getPermanentId(player2, "Captivating Vampire")))
                .isInstanceOf(IllegalStateException.class);
    }
}
