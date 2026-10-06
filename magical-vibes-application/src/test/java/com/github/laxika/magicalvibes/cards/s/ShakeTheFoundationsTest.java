package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShakeTheFoundations.class, GrizzlyBears.class, FugitiveWizard.class, SuntailHawk.class})
class ShakeTheFoundationsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature without flying, then draws a card")
    void damagesNonFlyersAndDrawsCard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new ShakeTheFoundations()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draws exactly one card on an empty battlefield without damaging players")
    void drawsOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new ShakeTheFoundations()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new FugitiveWizard()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLibraries.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shake the Foundations");
    }

    @Test
    @DisplayName("Draws a card when all creatures have flying and leaves them undamaged")
    void drawsWhenOnlyFlyersArePresent() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new ShakeTheFoundations()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanent(player1, "Suntail Hawk").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Suntail Hawk").getMarkedDamage()).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
