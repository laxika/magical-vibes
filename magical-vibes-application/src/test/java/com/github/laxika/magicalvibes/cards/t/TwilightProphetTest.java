package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwilightProphet.class, GrizzlyBears.class, Swamp.class})
class TwilightProphetTest extends BaseCardTest {

    @Test
    @DisplayName("With the city's blessing, reveals the top card, drains opponents, and puts it into hand")
    void blessingRevealsAndDrains() {
        harness.addToBattlefield(player1, new TwilightProphet());
        gd.playersWithCityBlessing.add(player1.getId());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginUpkeep(player1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Without the city's blessing, the upkeep ability does not resolve")
    void noBlessingDoesNotTriggerEffect() {
        harness.addToBattlefield(player1, new TwilightProphet());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginUpkeep(player1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An empty library causes no life change")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new TwilightProphet());
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginUpkeep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void revealedLandEntersHandWithoutChangingLife() {
        harness.addToBattlefield(player1, new TwilightProphet());
        gd.playersWithCityBlessing.add(player1.getId());
        Card topCard = new Swamp();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginUpkeep(player1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new TwilightProphet());
        gd.playersWithCityBlessing.add(player1.getId());
        Card topCard = new TwilightProphet();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void gettingBlessingAfterUpkeepBeginsDoesNotCreateTrigger() {
        harness.addToBattlefield(player1, new TwilightProphet());
        Card topCard = new TwilightProphet();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        gd.playersWithCityBlessing.add(player1.getId());
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void triggerStillResolvesAfterProphetLeavesBattlefield() {
        harness.addToBattlefield(player1, new TwilightProphet());
        gd.playersWithCityBlessing.add(player1.getId());
        Card topCard = new TwilightProphet();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void tenthPermanentGrantsBlessingAndBlessingPersistsBelowTen() {
        harness.addToBattlefield(player1, new TwilightProphet());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Swamp());
        }
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new Swamp());

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Swamp);
        Card topCard = new TwilightProphet();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginUpkeep(player1);

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }
    @Test
    void faceDownProphetDoesNotGrantBlessingAtTenPermanents() {
        var prophet = harness.addToBattlefieldAndReturn(player1, new TwilightProphet());
        prophet.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Swamp());
        }

        harness.enterBattlefieldAndReturn(player1, new Swamp());

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }
    private void beginUpkeep(Player player) {
        advanceToUpkeep(player);
        harness.passBothPriorities();
    }
}
