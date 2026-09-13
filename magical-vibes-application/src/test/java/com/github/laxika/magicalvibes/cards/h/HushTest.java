package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AbsoluteLaw;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Hush.class, AbsoluteLaw.class, AngelicChorus.class, ArgothianSwine.class})
class HushTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all enchantments controlled by both players")
    void destroysAllEnchantmentsControlledByBothPlayers() {
        harness.addToBattlefield(player1, new AbsoluteLaw());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player1, new ArgothianSwine());

        harness.castFromHand(player1, new Hush(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Absolute Law");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Argothian Swine");
    }

    @Test
    @DisplayName("Cycling discards Hush and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Hush()));
        harness.setLibrary(player1, List.of(new ArgothianSwine()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hush");
        harness.assertInHand(player1, "Argothian Swine");
    }
}
