package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloneCrafter.class, Forest.class, GrizzlyBears.class, Unsummon.class})
class CloneCrafterTest extends BaseCardTest {

    @Test
    void conjuresARecreatedRandomCreatureFromTheOpponentsLibrary() {
        Forest libraryLand = new Forest();
        GrizzlyBears libraryCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(libraryLand, libraryCreature));
        harness.setHand(player1, List.of(new CloneCrafter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card conjured = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(conjured.getId()).isNotEqualTo(libraryCreature.getId());
        assertThat(conjured.getOwnerId()).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryLand, libraryCreature);

        harness.forceActivePlayer(player1);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(conjured));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(conjured.getId()));
    }

    @Test
    void doesNotConjureFromAnEmptyOpponentLibrary() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CloneCrafter());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotUseTheControllersLibraryWhenOpponentHasNoCreatures() {
        Forest land = new Forest();
        GrizzlyBears ownCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownCreature));
        harness.setLibrary(player2, List.of(land));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CloneCrafter());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
    }

    @Test
    void anyColorPermissionSurvivesReturningTheConjuredCreatureToHand() {
        GrizzlyBears libraryCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(libraryCreature));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.enterBattlefieldAndReturn(player1, new CloneCrafter());
        harness.passBothPriorities();
        Card conjured = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(conjured));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(conjured);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCreature);
    }
}
