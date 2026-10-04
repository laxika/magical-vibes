package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BrassSecretary;
import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.p.PhyrexianNegator;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Eradicate.class, GoliathBeetle.class, BraidwoodCup.class, PhyrexianNegator.class,
        BrassSecretary.class, PsychogenicProbe.class})
class EradicateTest extends BaseCardTest {

    @Test
    @DisplayName("Can leave matching hidden-zone cards while exiling every graveyard copy")
    void canFailToFindHiddenZoneCopies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());
        GoliathBeetle handCopy = new GoliathBeetle();
        GoliathBeetle libraryCopy = new GoliathBeetle();
        GoliathBeetle graveyardCopy = new GoliathBeetle();
        harness.setHand(player2, List.of(handCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard(), graveyardCopy)
                .doesNotContain(handCopy, libraryCopy);
        assertThat(gd.playerHands.get(player2.getId())).contains(handCopy);
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
        harness.assertNotInGraveyard(player2, "Goliath Beetle");
    }

    @Test
    @DisplayName("A face-down target has no name and does not match its printed-name copies")
    void faceDownTargetDoesNotMatchPrintedName() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        GoliathBeetle handCopy = new GoliathBeetle();
        GoliathBeetle libraryCopy = new GoliathBeetle();
        GoliathBeetle graveyardCopy = new GoliathBeetle();
        harness.setHand(player2, List.of(handCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard())
                .doesNotContain(handCopy, libraryCopy, graveyardCopy);
        assertThat(gd.playerHands.get(player2.getId())).contains(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Psychogenic Probe"));
    }

    @Test
    @DisplayName("A stolen target goes to its owner's exile but its controller's zones are searched")
    void stolenTargetSearchesControllerRatherThanOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());
        gd.stolenCreatures.put(target.getId(), player1.getId());
        GoliathBeetle ownerCopy = new GoliathBeetle();
        GoliathBeetle controllerCopy = new GoliathBeetle();
        harness.setGraveyard(player1, List.of(ownerCopy));
        harness.setGraveyard(player2, List.of(controllerCopy));
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard()).doesNotContain(ownerCopy);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(controllerCopy).doesNotContain(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownerCopy);
        harness.assertNotInGraveyard(player2, "Goliath Beetle");
    }

    @Test
    @DisplayName("Exiles the target creature and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle()).getId();
        harness.setHand(player2, List.of(new GoliathBeetle(), new BraidwoodCup()));
        harness.setGraveyard(player2, List.of(new GoliathBeetle(), new BraidwoodCup()));

        harness.setLibrary(player2, List.of(new GoliathBeetle(), new BraidwoodCup()));

        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Goliath Beetle"))
                .hasSize(4);

        harness.assertNotInHand(player2, "Goliath Beetle");
        harness.assertNotInGraveyard(player2, "Goliath Beetle");
        harness.assertInHand(player2, "Braidwood Cup");
        harness.assertInGraveyard(player2, "Braidwood Cup");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goliath Beetle"))
                .anyMatch(c -> c.getName().equals("Braidwood Cup"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Braidwood Cup"));
    }

    @Test
    @DisplayName("Exiles a colorless creature because it is nonblack")
    void exilesColorlessCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BrassSecretary()).getId();
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Brass Secretary");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Brass Secretary"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Searches only the target creature's controller's zones")
    void onlySearchesTargetControllersZones() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle()).getId();
        harness.setHand(player1, List.of(new Eradicate(), new GoliathBeetle()));
        harness.setGraveyard(player1, List.of(new GoliathBeetle()));
        harness.setLibrary(player1, List.of(new GoliathBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Goliath Beetle"))
                .hasSize(1);
        harness.assertInHand(player1, "Goliath Beetle");
        harness.assertInGraveyard(player1, "Goliath Beetle");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goliath Beetle"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Goliath Beetle"));
    }

    @Test
    @DisplayName("Does not exile another same-name creature on the battlefield")
    void leavesOtherBattlefieldCopyAlone() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle()).getId();
        harness.addToBattlefield(player2, new GoliathBeetle());
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        assertThat(countPermanents(player2, "Goliath Beetle")).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Goliath Beetle"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Causes opponent-library-shuffle triggers when searching and shuffling")
    void triggersOpponentLibraryShuffleAbilities() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle()).getId();
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        assertThat(gd.stack)
                .anyMatch(entry -> entry.getCard().getName().equals("Psychogenic Probe"));
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle()).getId();
        harness.setHand(player2, List.of(new GoliathBeetle()));
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Goliath Beetle");
        harness.assertInGraveyard(player1, "Eradicate");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        UUID blackId = harness.addToBattlefieldAndReturn(player2, new PhyrexianNegator()).getId();
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup()).getId();
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }
}
