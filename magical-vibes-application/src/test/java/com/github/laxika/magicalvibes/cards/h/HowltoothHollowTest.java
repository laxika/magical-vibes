package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HowltoothHollow.class, GrizzlyBears.class, Plains.class, Shock.class, NamelessInversion.class})
class HowltoothHollowTest extends BaseCardTest {

    /** Puts Howltooth Hollow on the battlefield with {@code imprinted} exiled/imprinted on it. */
    private Permanent addHollowWithImprint(Card imprinted) {
        Permanent hollow = harness.addToBattlefieldAndReturn(player1, new HowltoothHollow());
        GameData gd = harness.getGameData();
        gd.setImprintedCard(hollow.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return hollow;
    }

    @Test
    @DisplayName("Plays the exiled card when each player has no cards in hand")
    void playsExiledCardWithEmptyHands() {
        GrizzlyBears bears = new GrizzlyBears();
        addHollowWithImprint(bears);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability -> offers "may play"
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the free-cast creature spell

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getImprintedCard(findPermanent(player1, "Howltooth Hollow").getCard())).isNull();
    }

    @Test
    @DisplayName("Does nothing while a player still has cards in hand")
    void doesNothingWithCardsInHand() {
        GrizzlyBears bears = new GrizzlyBears();
        addHollowWithImprint(bears);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock())); // opponent still holds a card
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability — condition not met

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may choice leaves the card exiled")
    void decliningLeavesCardExiled() {
        GrizzlyBears bears = new GrizzlyBears();
        addHollowWithImprint(bears);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Playing an exiled land counts as the land play for the turn")
    void playsExiledLandAsLandPlay() {
        Plains plains = new Plains();
        addHollowWithImprint(plains);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    void hideawayExilesOneOfTopFourFaceDownAndBottomsTheRest() {
        Plains first = new Plains();
        Plains second = new Plains();
        Plains third = new Plains();
        Plains fourth = new Plains();
        Plains fifth = new Plains();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new HowltoothHollow()));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        Permanent hollow = findPermanent(player1, "Howltooth Hollow");
        assertThat(hollow.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        GameData gd = harness.getGameData();
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
        assertThat(gd.getImprintedCard(hollow.getCard())).isSameAs(second);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(first, third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tapsForBlackManaWithoutUsingTheStack() {
        Permanent hollow = harness.addToBattlefieldAndReturn(player1, new HowltoothHollow());
        harness.forceActivePlayer(player1);

        harness.tapPermanent(player1, 0);

        assertThat(hollow.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.BLACK))
                .isEqualTo(1);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void hideawayWithOneCardExilesItWithoutAChoice() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.setHand(player1, List.of(new HowltoothHollow()));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.findExiledCard(plains.getId()).faceDown()).isTrue();
        assertThat(gd.getImprintedCard(findPermanent(player1, "Howltooth Hollow").getCard()))
                .isSameAs(plains);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void hideawayWithEmptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HowltoothHollow()));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Howltooth Hollow").isTapped()).isTrue();
    }

    @Test
    void controllersHandMustAlsoBeEmptyAtResolution() {
        addHollowWithImprint(new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new Plains()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    void checksHandsAtResolutionRatherThanActivation() {
        GrizzlyBears bears = new GrizzlyBears();
        addHollowWithImprint(bears);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player2, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotPlayExiledLandAfterUsingLandPlay() {
        Plains plains = new Plains();
        addHollowWithImprint(plains);
        harness.setHand(player1, List.of(new Plains()));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(harness.getGameData().findExiledCard(plains.getId())).isNotNull();
        assertThat(harness.getGameData().landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPlayExiledLandDuringOpponentsTurn() {
        Plains plains = new Plains();
        addHollowWithImprint(plains);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(harness.getGameData().findExiledCard(plains.getId())).isNotNull();
    }

    @Test
    void uncastableExiledSpellRemainsLinkedToHollow() {
        NamelessInversion inversion = new NamelessInversion();
        Permanent hollow = addHollowWithImprint(inversion);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(harness.getGameData().findExiledCard(inversion.getId())).isNotNull();
        assertThat(harness.getGameData().getImprintedCard(hollow.getCard())).isSameAs(inversion);
    }
}
