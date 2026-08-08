package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuashTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant spell and exiles every same-name copy from graveyard, hand, and library")
    void countersInstantAndExilesAllCopies() {
        Card castCopy = new Shock();
        harness.setHand(player1, List.of(castCopy, new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Shock());
        gd.playerDecks.get(player1.getId()).add(new Plains());

        harness.setHand(player2, List.of(new Quash()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());
        harness.passBothPriorities();

        // Countered — no damage dealt.
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();

        // All four Shocks (cast + hand + graveyard + library) exiled.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Shock"))
                .hasSize(4);

        harness.assertNotInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Quash()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Quash goes to its caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card castCopy = new Shock();
        harness.setHand(player1, List.of(castCopy));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Quash()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player2, "Quash");
    }
}
