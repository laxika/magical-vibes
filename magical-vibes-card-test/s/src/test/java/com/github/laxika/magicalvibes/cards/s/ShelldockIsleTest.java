package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({ShelldockIsle.class, GrizzlyBears.class, Plains.class, NamelessInversion.class})
class ShelldockIsleTest extends BaseCardTest {

    /** Puts Shelldock Isle on the battlefield with {@code imprinted} exiled/imprinted on it. */
    private Permanent addIsleWithImprint(Card imprinted) {
        Permanent isle = harness.addToBattlefieldAndReturn(player1, new ShelldockIsle());
        GameData gd = harness.getGameData();
        gd.setImprintedCard(isle.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return isle;
    }

    /** A library with {@code count} filler cards. */
    private List<Card> libraryOf(int count) {
        return java.util.stream.IntStream.range(0, count).mapToObj(i -> (Card) new GrizzlyBears()).toList();
    }

    @Test
    @DisplayName("Plays the exiled card when a library has twenty or fewer cards")
    void playsExiledCardWithSmallLibrary() {
        GrizzlyBears bears = new GrizzlyBears();
        addIsleWithImprint(bears);
        harness.setLibrary(player1, libraryOf(20));
        harness.setLibrary(player2, libraryOf(40));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability -> offers "may play"
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the free-cast creature spell

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getImprintedCard(findPermanent(player1, "Shelldock Isle").getCard())).isNull();
    }

    @Test
    @DisplayName("An opponent's small library also satisfies the condition")
    void opponentSmallLibrarySatisfies() {
        GrizzlyBears bears = new GrizzlyBears();
        addIsleWithImprint(bears);
        harness.setLibrary(player1, libraryOf(40));
        harness.setLibrary(player2, libraryOf(15));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing while every library has more than twenty cards")
    void doesNothingWithLargeLibraries() {
        GrizzlyBears bears = new GrizzlyBears();
        addIsleWithImprint(bears);
        harness.setLibrary(player1, libraryOf(40));
        harness.setLibrary(player2, libraryOf(40));
        harness.addMana(player1, ManaColor.BLUE, 1);
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
        addIsleWithImprint(bears);
        harness.setLibrary(player1, libraryOf(20));
        harness.setLibrary(player2, libraryOf(40));
        harness.addMana(player1, ManaColor.BLUE, 1);
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
    void entersTappedAndHidesOneOfTopFour() {
        ShelldockIsle isle = new ShelldockIsle();
        Plains first = new Plains();
        Plains second = new Plains();
        Plains third = new Plains();
        Plains fourth = new Plains();
        Plains fifth = new Plains();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(isle));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        assertThat(findPermanent(player1, "Shelldock Isle").isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.findExiledCard(first.getId()).faceDown()).isTrue();
        assertThat(gd.getImprintedCard(isle)).isSameAs(first);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(second, third, fourth);
    }

    @Test
    void tapProducesBlueManaWithoutUsingStack() {
        Permanent isle = harness.addToBattlefieldAndReturn(player1, new ShelldockIsle());
        harness.forceActivePlayer(player1);

        harness.tapPermanent(player1, 0);

        assertThat(isle.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void cannotPlayHiddenLandOnOpponentsTurn() {
        Plains plains = new Plains();
        addIsleWithImprint(plains);
        harness.setLibrary(player1, libraryOf(20));
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(harness.getGameData().findExiledCard(plains.getId())).isNotNull();
    }

    @Test
    void cannotPlayHiddenLandAfterLandAllowanceUsed() {
        Plains plains = new Plains();
        addIsleWithImprint(plains);
        harness.setLibrary(player1, libraryOf(20));
        harness.forceActivePlayer(player1);
        harness.getGameData().landsPlayedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(harness.getGameData().findExiledCard(plains.getId())).isNotNull();
    }

    @Test
    void uncastableHiddenSpellRemainsLinkedToIsle() {
        NamelessInversion inversion = new NamelessInversion();
        Permanent isle = addIsleWithImprint(inversion);
        harness.setLibrary(player1, libraryOf(20));
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (harness.getGameData().interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(harness.getGameData().findExiledCard(inversion.getId())).isNotNull();
        assertThat(harness.getGameData().getImprintedCard(isle.getCard())).isSameAs(inversion);
    }

    @Test
    void conditionIsCheckedWhenAbilityResolves() {
        addIsleWithImprint(new GrizzlyBears());
        harness.setLibrary(player1, libraryOf(21));
        harness.setLibrary(player2, libraryOf(40));
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.setLibrary(player1, libraryOf(20));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
