package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmugglersBuggy.class, GrizzlyBears.class})
class SmugglersBuggyTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway 4 exiles one card face down and bottoms the rest")
    void hideawayExilesOneCardAndBottomsTheRest() {
        Card chosen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new SmugglersBuggy());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent buggy = findPermanent(player1, "Smuggler's Buggy");
        assertThat(gd.getImprintedCard(buggy.getCard())).isSameAs(chosen);
        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("After combat damage, accepts the free cast and returns the Vehicle to hand")
    void castsExiledCardAndReturnsToHand() {
        GrizzlyBears exiled = new GrizzlyBears();
        Permanent buggy = addBuggyWithImprint(exiled);
        Permanent crew = addReadyPermanent(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crew.isTapped()).isTrue();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Smuggler's Buggy"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Smuggler's Buggy"));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the free cast leaves the Vehicle and exiled card in place")
    void decliningFreeCastLeavesVehicleAndCard() {
        GrizzlyBears exiled = new GrizzlyBears();
        Permanent buggy = addBuggyWithImprint(exiled);
        addReadyPermanent(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Smuggler's Buggy"));
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == exiled);
    }

    private Permanent addBuggyWithImprint(Card exiled) {
        Permanent buggy = harness.addToBattlefieldAndReturn(player1, new SmugglersBuggy());
        GameData gameData = harness.getGameData();
        gameData.setImprintedCard(buggy.getCard(), exiled);
        gameData.addToExile(player1.getId(), exiled, buggy.getId());
        buggy.setSummoningSick(false);
        return buggy;
    }

    private Permanent addReadyPermanent(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
