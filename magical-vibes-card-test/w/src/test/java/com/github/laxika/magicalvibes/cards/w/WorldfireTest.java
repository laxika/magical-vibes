package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Thragtusk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Worldfire.class, GrizzlyBears.class, Island.class, Peek.class, Shock.class, Thragtusk.class})
class WorldfireTest extends BaseCardTest {

    private void addCost() {
        // {6}{R}{R}{R}
        harness.addMana(player1, ManaColor.RED, 9);
    }

    @Test
    @DisplayName("Exiles every permanent on both battlefields, lands included")
    void exilesAllPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Worldfire()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles all cards from all hands and graveyards")
    void exilesAllHandsAndGraveyards() {
        harness.setHand(player1, new ArrayList<>(List.of(new Worldfire(), new GrizzlyBears())));
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Island())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        // Worldfire itself is put into its owner's graveyard after it finishes resolving,
        // so the caster's graveyard holds exactly that one card.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Worldfire");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each player's life total becomes 1")
    void setsEachPlayerLifeToOne() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 3);
        harness.setHand(player1, List.of(new Worldfire()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cards from each affected zone are exiled rather than merely removed")
    void cardsReachExileFromEveryAffectedZone() {
        GrizzlyBears creature = new GrizzlyBears();
        Island land = new Island();
        Peek handCard = new Peek();
        Shock graveyardCard = new Shock();
        Worldfire worldfire = new Worldfire();
        harness.addToBattlefield(player1, creature);
        harness.addToBattlefield(player2, land);
        harness.setHand(player1, List.of(worldfire));
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.findExiledCard(handCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(graveyardCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(worldfire.getId())).isNull();
        harness.assertInGraveyard(player1, "Worldfire");
    }

    @Test
    @DisplayName("Libraries and floating mana survive Worldfire")
    void preservesLibrariesAndFloatingMana() {
        Island firstLibraryCard = new Island();
        GrizzlyBears secondLibraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstLibraryCard));
        harness.setLibrary(player2, List.of(secondLibraryCard));
        harness.setHand(player1, List.of(new Worldfire()));
        harness.addMana(player1, ManaColor.RED, 11);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstLibraryCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondLibraryCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Leaves-the-battlefield triggers resolve after Worldfire finishes")
    void leavesTriggerCreatesTokenAfterWorldfire() {
        Thragtusk thragtusk = new Thragtusk();
        harness.addToBattlefield(player2, thragtusk);
        harness.castFromHand(player1, new Worldfire(), "{6}{R}{R}{R}");

        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(thragtusk.getId())).isNotNull();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player2, "Beast")).hasSize(1);
        assertThat(findPermanent(player2, "Beast").getCard().isToken()).isTrue();
        harness.assertLife(player2, 1);
    }
}
