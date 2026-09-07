package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IzoniCenterOfTheWeb.class, GrizzlyBears.class})
class IzoniCenterOfTheWebTest extends BaseCardTest {

    @Test
    void entersAndMayCollectEvidenceToCreateSpiders() {
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence));
        harness.setHand(player1, List.of(new IzoniCenterOfTheWeb()));
        addManaForIzoni();

        harness.castCreature(player1, 0);
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

    private void addManaForIzoni() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
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
