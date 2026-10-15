package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EidolonOfBlossoms.class, FontOfFertility.class, GoldenHind.class, Hubris.class})
class EidolonOfBlossomsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when Eidolon of Blossoms enters")
    void ownEntryTriggers() {
        harness.castFromHand(player1, new EidolonOfBlossoms(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws a card when another enchantment enters under your control")
    void anotherEnchantmentEntryTriggers() {
        harness.addToBattlefield(player1, new EidolonOfBlossoms());
        harness.castFromHand(player1, new FontOfFertility(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when a non-enchantment creature enters under your control")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new EidolonOfBlossoms());
        harness.castFromHand(player1, new GoldenHind(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's enchantment enters")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new EidolonOfBlossoms());
        harness.setHand(player1, List.of(new GoldenHind()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new FontOfFertility(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An entering second Eidolon triggers itself and the existing Eidolon once each")
    void secondEidolonTriggersBoth() {
        harness.addToBattlefield(player1, new EidolonOfBlossoms());
        harness.castFromHand(player1, new EidolonOfBlossoms(), "{2}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Constellation triggers when an enchantment enters without being cast")
    void enchantmentEnteringWithoutBeingCastTriggers() {
        harness.addToBattlefield(player1, new EidolonOfBlossoms());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new FontOfFertility());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw trigger resolves after Eidolon leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        var eidolon = harness.addToBattlefieldAndReturn(player1, new EidolonOfBlossoms());
        harness.castFromHand(player1, new FontOfFertility(), "{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, eidolon.getId());
        harness.assertNotOnBattlefield(player1, "Eidolon of Blossoms");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
