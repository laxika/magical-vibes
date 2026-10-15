package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LureboundScarecrow.class, Shock.class, PaintersServant.class})
class LureboundScarecrowTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Survives while controlling a permanent of the chosen color")
    void survivesWhileControllingChosenColor() {
        harness.addToBattlefield(player1, createCreature("Green Bear", 2, 2, CardColor.GREEN));
        harness.castFromHand(player1, new LureboundScarecrow(), "{3}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        // No state trigger fires — Scarecrow stays.
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lurebound Scarecrow");
    }

    @Test
    @DisplayName("Sacrificed when controlling no permanent of the chosen color")
    void sacrificedWhenNoPermanentOfChosenColor() {
        harness.castFromHand(player1, new LureboundScarecrow(), "{3}");
        harness.passBothPriorities();
        // Scarecrow itself is a colorless artifact creature, so choosing any color
        // leaves the controller with no permanents of that color → state trigger sacrifices it.
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lurebound Scarecrow");
        harness.assertInGraveyard(player1, "Lurebound Scarecrow");
    }

    @Test
    @DisplayName("Sacrificed once the last permanent of the chosen color leaves")
    void sacrificedWhenLastPermanentOfColorLeaves() {
        Card bear = createCreature("Green Bear", 2, 1, CardColor.GREEN);
        harness.addToBattlefield(player1, bear);
        harness.castFromHand(player1, new LureboundScarecrow(), "{3}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.assertOnBattlefield(player1, "Lurebound Scarecrow");

        // Kill the only green permanent — the state trigger now fires.
        UUID bearId = harness.getPermanentId(player1, "Green Bear");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearId);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Lurebound Scarecrow");
        harness.assertInGraveyard(player1, "Lurebound Scarecrow");
    }

    @Test
    @DisplayName("Opponent's permanent of the chosen color does not keep it alive")
    void opponentPermanentDoesNotCount() {
        harness.addToBattlefield(player2, createCreature("Green Bear", 2, 2, CardColor.GREEN));
        harness.castFromHand(player1, new LureboundScarecrow(), "{3}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        // Only the opponent controls a green permanent — "you control" is not satisfied.
        harness.passBothPriorities(); // state trigger onto the stack
        harness.passBothPriorities(); // resolve it
        harness.assertNotOnBattlefield(player1, "Lurebound Scarecrow");
        harness.assertInGraveyard(player1, "Lurebound Scarecrow");
    }

    @Test
    @DisplayName("Continuous color grants prevent the sacrifice trigger")
    void survivesWithPaintersServantColorGrant() {
        harness.castFromHand(player1, new PaintersServant(), "{2}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.castFromHand(player1, new LureboundScarecrow(), "{3}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lurebound Scarecrow");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lurebound Scarecrow");
        harness.assertNotInGraveyard(player1, "Lurebound Scarecrow");
    }

    @Test
    @DisplayName("An opponent's Painter's Servant colors Scarecrow itself and keeps it alive")
    void survivesWithOpponentsPaintersServant() {
        harness.addToBattlefieldAndReturn(player2, new PaintersServant())
                .setChosenColor(CardColor.GREEN);
        harness.castFromHand(player1, new LureboundScarecrow(), "{3}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lurebound Scarecrow");
        harness.assertNotInGraveyard(player1, "Lurebound Scarecrow");
    }
}
