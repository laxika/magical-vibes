package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CoppercoatVanguard;
import com.github.laxika.magicalvibes.cards.g.GoldForgedThopteryx;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JirinaDauntlessGeneral.class, CoppercoatVanguard.class, GoldForgedThopteryx.class})
class JirinaDauntlessGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("When Jirina enters, it exiles target player's graveyard")
    void exilesTargetPlayersGraveyard() {
        harness.setGraveyard(player2, List.of(new GoldForgedThopteryx(), new GoldForgedThopteryx()));
        harness.enterBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Sacrificing Jirina protects your Humans until end of turn")
    void sacrificesAndProtectsYourHumans() {
        harness.addToBattlefield(player1, new JirinaDauntlessGeneral());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new CoppercoatVanguard());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GoldForgedThopteryx());
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new CoppercoatVanguard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Jirina, Dauntless General");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, human, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void sacrificeIsPaidBeforeProtectionResolves() {
        harness.addToBattlefield(player1, new JirinaDauntlessGeneral());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new CoppercoatVanguard());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Jirina, Dauntless General");
        harness.assertInGraveyard(player1, "Jirina, Dauntless General");
        assertThat(gqs.hasKeyword(gd, human, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void protectsHumansPresentAtResolutionButNotThoseAddedLater() {
        harness.addToBattlefield(player1, new JirinaDauntlessGeneral());
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new CoppercoatVanguard());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new CoppercoatVanguard());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void canExileOwnGraveyardWithoutAffectingOpponent() {
        harness.setGraveyard(player1, List.of(new CoppercoatVanguard()));
        harness.setGraveyard(player2, List.of(new GoldForgedThopteryx()));
        harness.enterBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void emptyGraveyardIsLegalAndCardsAddedBeforeResolutionAreExiled() {
        harness.enterBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setGraveyard(player2, List.of(new CoppercoatVanguard()));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    @Test
    void enterTriggerStillResolvesAfterJirinaIsSacrificed() {
        harness.enterBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Jirina, Dauntless General");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Jirina, Dauntless General");
    }

    @Test
    void emptyGraveyardCanBeTargetedAndResolvesWithoutExilingAnything() {
        harness.enterBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
