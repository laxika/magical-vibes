package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    private boolean controlsScarecrow(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Lurebound Scarecrow"));
    }

    @Test
    @DisplayName("Survives while controlling a permanent of the chosen color")
    void survivesWhileControllingChosenColor() {
        harness.addToBattlefield(player1, createCreature("Green Bear", 2, 2, CardColor.GREEN));
        harness.setHand(player1, List.of(new LureboundScarecrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        // No state trigger fires — Scarecrow stays.
        assertThat(gd.stack).isEmpty();
        assertThat(controlsScarecrow(player1)).isTrue();
    }

    @Test
    @DisplayName("Sacrificed when controlling no permanent of the chosen color")
    void sacrificedWhenNoPermanentOfChosenColor() {
        harness.setHand(player1, List.of(new LureboundScarecrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        // Scarecrow itself is a colorless artifact creature, so choosing any color
        // leaves the controller with no permanents of that color → state trigger sacrifices it.
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();

        assertThat(controlsScarecrow(player1)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lurebound Scarecrow"));
    }

    @Test
    @DisplayName("Sacrificed once the last permanent of the chosen color leaves")
    void sacrificedWhenLastPermanentOfColorLeaves() {
        Card bear = createCreature("Green Bear", 2, 1, CardColor.GREEN);
        harness.addToBattlefield(player1, bear);
        harness.setHand(player1, List.of(new LureboundScarecrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        assertThat(controlsScarecrow(player1)).isTrue();

        // Kill the only green permanent — the state trigger now fires.
        UUID bearId = harness.getPermanentId(player1, "Green Bear");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearId);

        harness.passBothPriorities();
        assertThat(controlsScarecrow(player1)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lurebound Scarecrow"));
    }

    @Test
    @DisplayName("Opponent's permanent of the chosen color does not keep it alive")
    void opponentPermanentDoesNotCount() {
        harness.addToBattlefield(player2, createCreature("Green Bear", 2, 2, CardColor.GREEN));
        harness.setHand(player1, List.of(new LureboundScarecrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        // Only the opponent controls a green permanent — "you control" is not satisfied.
        harness.passBothPriorities(); // state trigger onto the stack
        harness.passBothPriorities(); // resolve it
        assertThat(controlsScarecrow(player1)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lurebound Scarecrow"));
    }
}
