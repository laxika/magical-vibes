package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReunionOfTheHouse.class, GrizzlyBears.class, HillGiant.class, Tarmogoyf.class})
class ReunionOfTheHouseTest extends BaseCardTest {

    @Test
    @DisplayName("Returns targeted creature cards with total power 10 or less and exiles itself")
    void returnsCreatureCardsWithinTotalPowerLimit() {
        Card bear = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card secondBear = new GrizzlyBears();
        bear.setPower(2);
        hillGiant.setPower(3);
        secondBear.setPower(2);
        harness.setGraveyard(player1, List.of(bear, hillGiant, secondBear));
        prepareSpell();

        harness.castSorcery(player1, 0,
                List.of(bear.getId(), hillGiant.getId(), secondBear.getId()));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(bear.getId(), hillGiant.getId(), secondBear.getId());
        assertThat(gameData.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Reunion of the House"));
    }

    @Test
    @DisplayName("Rejects a target selection whose total power exceeds 10")
    void rejectsSelectionAboveTotalPowerLimit() {
        List<Card> hillGiants = List.of(new HillGiant(), new HillGiant(), new HillGiant(), new HillGiant());
        hillGiants.forEach(card -> card.setPower(3));
        harness.setGraveyard(player1, hillGiants);
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                hillGiants.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power cannot exceed 10");
    }

    @Test
    void returnsCreaturesWithExactlyTenTotalPower() {
        List<Card> bears = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, bears);
        prepareSpell();

        harness.castSorcery(player1, 0, bears.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrderElementsOf(bears.stream().map(Card::getId).toList());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void canResolveWithoutTargetsAndExilesItself() {
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        prepareSpell();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof ReunionOfTheHouse);
    }

    @Test
    void returnsRemainingLegalTargetsWhenOneTargetLeavesGraveyard() {
        Card bear = new GrizzlyBears();
        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(bear, giant));
        prepareSpell();

        harness.castSorcery(player1, 0, List.of(bear.getId(), giant.getId()));
        harness.setGraveyard(player1, List.of(bear));
        harness.setExile(player1, List.of(giant));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof ReunionOfTheHouse);
    }

    @Test
    void goesToGraveyardInsteadOfExileWhenAllTargetsBecomeIllegal() {
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        prepareSpell();

        harness.castSorcery(player1, 0, List.of(bear.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bear));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reunion of the House");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card instanceof ReunionOfTheHouse);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void rejectsCreatureCardsInOpponentsGraveyard() {
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bear));
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNoncreatureCardsInOwnGraveyard() {
        Card sorcery = new ReunionOfTheHouse();
        harness.setGraveyard(player1, List.of(sorcery));
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(sorcery.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({ReunionOfTheHouse.class, Tarmogoyf.class})
    void countsCharacteristicDefinedPowerInGraveyardWhenChoosingTargets() {
        List<Card> creatures = java.util.stream.IntStream.range(0, 11)
                .mapToObj(index -> (Card) new Tarmogoyf()).toList();
        harness.setGraveyard(player1, creatures);
        harness.setGraveyard(player2, List.of());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                creatures.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power cannot exceed 10");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ReunionOfTheHouse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
