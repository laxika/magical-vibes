package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shadowfeed.class, DregscapeZombie.class})
class ShadowfeedTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target card from a graveyard and controller gains 3 life")
    void exilesCardAndGainsLife() {
        Card bears = new DregscapeZombie();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new Shadowfeed()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Dregscape Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dregscape Zombie"));
        harness.assertLife(player1, lifeBefore + 3);
    }

    @Test
    @DisplayName("Can exile a card from own graveyard")
    void exilesFromOwnGraveyard() {
        Card bears = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Shadowfeed()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dregscape Zombie"));
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new Shadowfeed()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile a noncreature card without exiling other cards")
    void exilesNoncreatureCardOnly() {
        Card target = new Shadowfeed();
        Card other = new DregscapeZombie();
        harness.setGraveyard(player2, List.of(target, other));
        harness.setHand(player1, List.of(new Shadowfeed()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertNotInGraveyard(player2, "Shadowfeed");
        harness.assertInGraveyard(player2, "Dregscape Zombie");
        harness.assertInGraveyard(player1, "Shadowfeed");
        harness.assertLife(player1, lifeBefore + 3);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("Does not gain life when the only target is exiled in response")
    void doesNotGainLifeWhenTargetBecomesIllegal() {
        Card target = new DregscapeZombie();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new Shadowfeed()));
        harness.setHand(player2, List.of(new Shadowfeed()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertNotInGraveyard(player2, "Dregscape Zombie");
        harness.assertInGraveyard(player1, "Shadowfeed");
        harness.assertInGraveyard(player2, "Shadowfeed");
        harness.assertLife(player1, lifeBefore);
        harness.assertLife(player2, opponentLifeBefore + 3);
        assertThat(gd.stack).isEmpty();
    }
}
