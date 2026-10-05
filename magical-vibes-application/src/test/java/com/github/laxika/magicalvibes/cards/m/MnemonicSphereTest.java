package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.w.WalkingSkyscraper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MnemonicSphere.class, BambooGroveArcher.class, WalkingSkyscraper.class})
class MnemonicSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Mnemonic Sphere draws two cards")
    void sacrificesAndDrawsTwoCards() {
        MnemonicSphere sphere = new MnemonicSphere();
        harness.addToBattlefield(player1, sphere);
        harness.setLibrary(player1, List.of(new BambooGroveArcher(), new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, sphere.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mnemonic Sphere");
        harness.assertInHand(player1, "Bamboo Grove Archer");
        harness.assertInHand(player1, "Walking Skyscraper");
    }

    @Test
    @DisplayName("Channeling Mnemonic Sphere draws a card")
    void channelsAndDrawsOneCard() {
        harness.setHand(player1, List.of(new MnemonicSphere()));
        harness.setLibrary(player1, List.of(new BambooGroveArcher()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mnemonic Sphere");
        harness.assertInHand(player1, "Bamboo Grove Archer");
    }

    @Test
    @DisplayName("A tapped Sphere is sacrificed as a cost before its draw ability resolves")
    void tappedSpherePaysSacrificeBeforeDrawing() {
        var sphere = harness.addToBattlefieldAndReturn(player1, new MnemonicSphere());
        sphere.setTapped(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BambooGroveArcher(), new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Mnemonic Sphere");
        harness.assertInGraveyard(player1, "Mnemonic Sphere");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Bamboo Grove Archer");
        harness.assertInHand(player1, "Walking Skyscraper");
    }

    @Test
    @DisplayName("Channel discards the Sphere as a cost before drawing exactly one card")
    void channelPaysDiscardBeforeDrawing() {
        harness.setHand(player1, List.of(new MnemonicSphere()));
        harness.setLibrary(player1, List.of(new BambooGroveArcher(), new WalkingSkyscraper()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Mnemonic Sphere");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Bamboo Grove Archer");
        harness.assertNotInHand(player1, "Walking Skyscraper");
    }

    @Test
    @DisplayName("The battlefield ability cannot sacrifice the Sphere without blue mana")
    void cannotSacrificeWithoutBlueMana() {
        harness.addToBattlefield(player1, new MnemonicSphere());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mnemonic Sphere");
        harness.assertNotInGraveyard(player1, "Mnemonic Sphere");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel cannot discard the Sphere without blue mana")
    void cannotChannelWithoutBlueMana() {
        harness.setHand(player1, List.of(new MnemonicSphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mnemonic Sphere");
        harness.assertNotInGraveyard(player1, "Mnemonic Sphere");
        assertThat(gd.stack).isEmpty();
    }
}
