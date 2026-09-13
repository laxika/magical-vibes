package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MnemonicSphere.class, GrizzlyBears.class, HillGiant.class})
class MnemonicSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Mnemonic Sphere draws two cards")
    void sacrificesAndDrawsTwoCards() {
        MnemonicSphere sphere = new MnemonicSphere();
        harness.addToBattlefield(player1, sphere);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, sphere.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mnemonic Sphere");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Channeling Mnemonic Sphere draws a card")
    void channelsAndDrawsOneCard() {
        harness.setHand(player1, List.of(new MnemonicSphere()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mnemonic Sphere");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
