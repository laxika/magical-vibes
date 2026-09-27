package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        TezzeretMasterOfTheBridge.class,
        Forest.class,
        GrizzlyBears.class,
        MindStone.class
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

    private Permanent addReadyTezzeret(int loyalty) {
        Permanent permanent = new Permanent(new TezzeretMasterOfTheBridge());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
