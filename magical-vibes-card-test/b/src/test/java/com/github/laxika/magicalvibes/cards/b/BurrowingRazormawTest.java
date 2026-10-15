package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurrowingRazormaw.class, Forest.class, Shock.class})
class BurrowingRazormawTest extends BaseCardTest {

    @Test
    @DisplayName("When Burrowing Razormaw dies, its controller mills four cards")
    void deathMillsFourCards() {
        harness.addToBattlefield(player1, new BurrowingRazormaw());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID razormawId = harness.getPermanentId(player1, "Burrowing Razormaw");
        harness.castInstant(player2, 0, razormawId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Forest"))
                .hasSize(4);
    }

    @Test
    @DisplayName("Death trigger mills exactly the top four cards only when it resolves")
    void millsOnlyTopFourOnResolution() {
        BurrowingRazormaw razormaw = new BurrowingRazormaw();
        harness.addToBattlefield(player1, razormaw);
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        Forest remaining = new Forest();
        Forest opponentsCard = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, remaining));
        harness.setLibrary(player2, List.of(opponentsCard));

        castLethalShock();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(razormaw);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(first, second, third, fourth, remaining);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(razormaw, first, second, third, fourth)
                .doesNotContain(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentsCard);
    }

    @Test
    @DisplayName("Death trigger mills all remaining cards when fewer than four remain")
    void millsShortLibrary() {
        harness.addToBattlefield(player1, new BurrowingRazormaw());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        castLethalShock();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Death trigger resolves harmlessly with an empty library")
    void emptyLibrary() {
        harness.addToBattlefield(player1, new BurrowingRazormaw());
        harness.setLibrary(player1, List.of());

        castLethalShock();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private void castLethalShock() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Burrowing Razormaw"));
    }
}
