package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.d.DeathDenied;
import com.github.laxika.magicalvibes.cards.g.GhostLitRedeemer;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeScout;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CelestialKirin.class, ArabaMothrider.class, DeathDenied.class, GhostLitRedeemer.class,
        SakuraTribeScout.class, SpiritualVisit.class})
class CelestialKirinTest extends BaseCardTest {

    @Test
    @DisplayName("An Arcane spell destroys all permanents with its mana value")
    void arcaneSpellDestroysMatchingManaValuePermanents() {
        harness.addToBattlefield(player1, new CelestialKirin());
        harness.addToBattlefield(player1, new GhostLitRedeemer());
        harness.addToBattlefield(player2, new GhostLitRedeemer());
        harness.addToBattlefield(player2, new ArabaMothrider());

        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost-Lit Redeemer");
        harness.assertInGraveyard(player2, "Ghost-Lit Redeemer");
        harness.assertOnBattlefield(player2, "Araba Mothrider");
        harness.assertOnBattlefield(player1, "Celestial Kirin");
    }

    @Test
    @DisplayName("A Spirit spell destroys permanents with its mana value")
    void spiritSpellDestroysMatchingManaValuePermanents() {
        harness.addToBattlefield(player1, new CelestialKirin());
        harness.addToBattlefield(player1, new SakuraTribeScout());
        harness.addToBattlefield(player2, new SakuraTribeScout());
        harness.addToBattlefield(player2, new ArabaMothrider());

        harness.castFromHand(player1, new GhostLitRedeemer(), "{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sakura-Tribe Scout");
        harness.assertInGraveyard(player2, "Sakura-Tribe Scout");
        harness.assertOnBattlefield(player2, "Araba Mothrider");
        harness.assertOnBattlefield(player1, "Celestial Kirin");
    }

    @Test
    @DisplayName("A spell that is neither a Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new CelestialKirin());
        harness.addToBattlefield(player2, new ArabaMothrider());

        harness.castFromHand(player1, new ArabaMothrider(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Araba Mothrider");
        harness.assertOnBattlefield(player1, "Celestial Kirin");
    }

    @Test
    @DisplayName("An opponent casting a Spirit or Arcane spell does not trigger it")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new CelestialKirin());
        harness.addToBattlefield(player1, new SakuraTribeScout());

        harness.castFromHand(player2, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Celestial Kirin");
        harness.assertOnBattlefield(player1, "Sakura-Tribe Scout");
    }

    @Test
    @DisplayName("An X Arcane spell uses its chosen mana value")
    void xArcaneSpellUsesChosenManaValue() {
        harness.addToBattlefield(player1, new CelestialKirin());
        harness.addToBattlefield(player1, new ArabaMothrider());
        GhostLitRedeemer first = new GhostLitRedeemer();
        SakuraTribeScout second = new SakuraTribeScout();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DeathDenied()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Celestial Kirin");
        harness.assertOnBattlefield(player1, "Araba Mothrider");
    }
}
