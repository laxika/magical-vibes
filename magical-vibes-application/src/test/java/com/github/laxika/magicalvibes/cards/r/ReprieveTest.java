package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reprieve.class, Combust.class, Forest.class, GrizzlyBears.class, SavannahLions.class, Island.class, JhovallRider.class})
class ReprieveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a spell to its owner's hand and draws a card")
    void returnsSpellToOwnersHandAndDraws() {
        harness.setLibrary(player2, List.of(new Forest()));

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Reprieve()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Reprieve");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Returns an uncounterable spell and draws a card")
    void returnsUncounterableSpellAndDraws() {
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new SavannahLions());

        Combust combust = new Combust();
        harness.setHand(player1, List.of(combust));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Reprieve()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Savannah Lions"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, combust.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Combust");
        harness.assertOnBattlefield(player2, "Savannah Lions");
        harness.assertInGraveyard(player2, "Reprieve");
        assertThat(harness.getGameData().playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Reprieve()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsTargetSpellToItsOwnersHandAndDrawsACard() {
        JhovallRider rider = new JhovallRider();
        harness.castFromHand(player1, rider, "{4}{W}");

        Island drawnCard = new Island();
        harness.setLibrary(player2, List.of(drawnCard));
        Reprieve reprieve = new Reprieve();
        harness.setHand(player2, List.of(reprieve));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, rider.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jhovall Rider");
        harness.assertInHand(player2, "Island");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(reprieve.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetAPermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Reprieve reprieve = new Reprieve();
        harness.setHand(player2, List.of(reprieve));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(reprieve);
        assertThat(gd.stack).isEmpty();
    }
}
