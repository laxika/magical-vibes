package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IzoniCenterOfTheWeb.class, GrizzlyBears.class, ZulaportCutthroat.class})
class IzoniCenterOfTheWebTest extends BaseCardTest {

    @Test
    void entersAndMayCollectEvidenceToCreateSpiders() {
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence));
        harness.castFromHand(player1, new IzoniCenterOfTheWeb(), "{4}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId(), secondEvidence.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(token.getCard().getColors())
                            .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIDER);
                    assertThat(token.getCard().getKeywords())
                            .containsExactlyInAnyOrder(Keyword.REACH, Keyword.MENACE);
                });
    }

    @Test
    void attacksAndMayDeclineToCollectEvidence() {
        addCreatureReady(player1, new IzoniCenterOfTheWeb());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void sacrificesFourTokensToSurveilDrawAndGainLife() {
        harness.addToBattlefield(player1, new IzoniCenterOfTheWeb());
        harness.addToBattlefield(player1, tokenCard("Token 1"));
        harness.addToBattlefield(player1, tokenCard("Token 2"));
        harness.addToBattlefield(player1, tokenCard("Token 3"));
        harness.addToBattlefield(player1, tokenCard("Token 4"));

        Card scriedFirst = new GrizzlyBears();
        Card scriedSecond = new GrizzlyBears();
        Card drawnFirst = new GrizzlyBears();
        Card drawnSecond = new GrizzlyBears();
        harness.setLibrary(player1, List.of(scriedFirst, scriedSecond, drawnFirst, drawnSecond));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(scriedFirst, scriedSecond);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void attackCollectsMoreThanFourManaValueAndExilesOnlyChosenEvidence() {
        Card evidence = new IzoniCenterOfTheWeb();
        Card unchosen = new IzoniCenterOfTheWeb();
        Card opponentsEvidence = new IzoniCenterOfTheWeb();
        harness.setGraveyard(player1, List.of(evidence, unchosen));
        harness.setGraveyard(player2, List.of(opponentsEvidence));
        addCreatureReady(player1, new IzoniCenterOfTheWeb());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsEvidence);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(evidence);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    void insufficientEvidenceDoesNotCreateSpidersOrExileCards() {
        Card evidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evidence));
        harness.castFromHand(player1, new IzoniCenterOfTheWeb(), "{4}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void decliningEvidenceLeavesAvailableCardsInGraveyard() {
        Card evidence = new IzoniCenterOfTheWeb();
        harness.setGraveyard(player1, List.of(evidence));
        harness.castFromHand(player1, new IzoniCenterOfTheWeb(), "{4}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void sacrificesNoncreatureTokensAsCostAndSurveilsIntoGraveyardBeforeDrawing() {
        harness.addToBattlefield(player1, new IzoniCenterOfTheWeb());
        for (int i = 0; i < 4; i++) {
            Card token = tokenCard("Treasure " + i);
            token.setType(CardType.ARTIFACT);
            token.setSubtypes(List.of(CardSubtype.TREASURE));
            harness.addToBattlefield(player1, token);
        }
        Card surveilledFirst = new IzoniCenterOfTheWeb();
        Card surveilledSecond = new IzoniCenterOfTheWeb();
        Card drawnFirst = new IzoniCenterOfTheWeb();
        Card drawnSecond = new IzoniCenterOfTheWeb();
        harness.setLibrary(player1, List.of(surveilledFirst, surveilledSecond, drawnFirst, drawnSecond));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(surveilledFirst, surveilledSecond);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond);
        harness.assertLife(player1, 22);
    }

    @Test
    void sacrificedTokenCopiesSeeAllFourSimultaneousDeaths() {
        harness.addToBattlefield(player1, new IzoniCenterOfTheWeb());
        for (int i = 0; i < 4; i++) {
            Card tokenCopy = new ZulaportCutthroat();
            tokenCopy.setToken(true);
            harness.addToBattlefield(player1, tokenCopy);
        }
        Card first = new IzoniCenterOfTheWeb();
        Card second = new IzoniCenterOfTheWeb();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.assertLife(player2, 4);
        harness.assertLife(player1, 38);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void cannotPayWithThreeOwnTokensAndAnOpponentsTokenOrNontoken() {
        harness.addToBattlefield(player1, new IzoniCenterOfTheWeb());
        harness.addToBattlefield(player1, tokenCard("Token 1"));
        harness.addToBattlefield(player1, tokenCard("Token 2"));
        harness.addToBattlefield(player1, tokenCard("Token 3"));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, tokenCard("Opponent's token"));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        harness.assertLife(player1, 20);
    }

    private static Card tokenCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setColors(List.of(CardColor.GREEN));
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        card.setSubtypes(List.of(CardSubtype.SAPROLING));
        return card;
    }
}
