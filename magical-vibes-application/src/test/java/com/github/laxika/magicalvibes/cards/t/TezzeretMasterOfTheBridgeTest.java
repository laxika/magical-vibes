package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        TezzeretMasterOfTheBridge.class,
        Forest.class,
        GrizzlyBears.class,
        MindStone.class,
        Ornithopter.class
})
class TezzeretMasterOfTheBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces creature spell costs")
    void affinityReducesCreatureSpellCost() {
        addReadyTezzeret(3);
        harness.addToBattlefield(player1, new MindStone());
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
    }

    @Test
    void affinityReducesPlaneswalkerSpellCost() {
        addReadyTezzeret(5);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new MindStone());
        }
        Card spell = new TezzeretMasterOfTheBridge();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(spell.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    void tezzeretDoesNotGrantAffinityBeforeEnteringBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new MindStone());
        }
        harness.setHand(player1, List.of(new TezzeretMasterOfTheBridge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void affinityDoesNotReduceColoredMana() {
        addReadyTezzeret(5);
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new MindStone());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void affinityDoesNotCountOpponentsArtifacts() {
        addReadyTezzeret(5);
        harness.addToBattlefield(player2, new MindStone());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void affinityDoesNotReduceNoncreatureArtifactSpells() {
        addReadyTezzeret(5);
        harness.addToBattlefield(player1, new MindStone());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Plus two deals damage and gains life equal to artifact count")
    void plusTwoDealsDamageAndGainsLife() {
        Permanent tezzeret = addReadyTezzeret(3);
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new MindStone());

        harness.activateAbility(player1, battlefieldIndex(tezzeret), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusTwoCountsArtifactsAtResolutionAndOnlyThoseYouControl() {
        Permanent tezzeret = addReadyTezzeret(5);
        harness.addToBattlefield(player2, new MindStone());

        harness.activateAbility(player1, battlefieldIndex(tezzeret), 0, null, null);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void plusTwoWithNoArtifactsChangesOnlyLoyalty() {
        Permanent tezzeret = addReadyTezzeret(5);

        harness.activateAbility(player1, battlefieldIndex(tezzeret), 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("Minus three returns a target artifact card")
    void minusThreeReturnsArtifactCard() {
        Permanent tezzeret = addReadyTezzeret(3);
        Card artifact = new MindStone();
        harness.setGraveyard(player1, List.of(artifact));

        harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(tezzeret), 1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Minus three rejects a non-artifact card")
    void minusThreeRejectsNonArtifactCard() {
        Permanent tezzeret = addReadyTezzeret(3);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(tezzeret), 1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusThreeRejectsOpponentsArtifactCard() {
        Permanent tezzeret = addReadyTezzeret(5);
        Card artifact = new MindStone();
        harness.setGraveyard(player2, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(tezzeret), 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusThreeDoesNotReturnTargetThatLeftGraveyard() {
        Permanent tezzeret = addReadyTezzeret(5);
        Card artifact = new MindStone();
        harness.setGraveyard(player1, List.of(artifact));

        harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(tezzeret), 1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Minus eight puts every exiled artifact from the top ten onto the battlefield")
    void minusEightPutsArtifactsOntoBattlefield() {
        Permanent tezzeret = addReadyTezzeret(8);
        List<Card> topCards = List.of(
                new GrizzlyBears(), new MindStone(), new Forest(), new MindStone(), new Forest(),
                new GrizzlyBears(), new Forest(), new MindStone(), new Forest(), new GrizzlyBears());
        List<Card> artifacts = topCards.stream().filter(card -> card instanceof MindStone).toList();
        List<Card> nonArtifacts = topCards.stream().filter(card -> !(card instanceof MindStone)).toList();
        harness.setLibrary(player1, topCards);

        harness.activateAbility(player1, battlefieldIndex(tezzeret), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard().getId()))
                .containsAll(artifacts.stream().map(Card::getId).toList());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(nonArtifacts.stream().map(Card::getId).toList());
    }

    @Test
    void minusEightLeavesCardsBelowTopTenInLibrary() {
        Permanent tezzeret = addReadyTezzeret(8);
        List<Card> topCards = IntStream.range(0, 10)
                .mapToObj(i -> (Card) new Forest()).toList();
        Card eleventh = new MindStone();
        List<Card> library = new ArrayList<>(topCards);
        library.add(eleventh);
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, battlefieldIndex(tezzeret), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eleventh);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        harness.assertNotOnBattlefield(player1, "Mind Stone");
    }

    @Test
    void minusEightHandlesShortLibraryAndArtifactCreatures() {
        Permanent tezzeret = addReadyTezzeret(8);
        Card artifact = new Ornithopter();
        Card nonArtifact = new Forest();
        harness.setLibrary(player1, List.of(artifact, nonArtifact));

        harness.activateAbility(player1, battlefieldIndex(tezzeret), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(nonArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(tezzeret.getCard().getId()));
    }

    private Permanent addReadyTezzeret(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new TezzeretMasterOfTheBridge());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
