package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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
        addBuggyWithImprint(exiled);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crew.isTapped()).isTrue();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Smuggler's Buggy");
        harness.assertInHand(player1, "Smuggler's Buggy");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the free cast leaves the Vehicle and exiled card in place")
    void decliningFreeCastLeavesVehicleAndCard() {
        GrizzlyBears exiled = new GrizzlyBears();
        addBuggyWithImprint(exiled);
        addCreatureReady(player1, new GrizzlyBears());

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
        gd.setImprintedCard(buggy.getCard(), exiled);
        gd.addToExile(player1.getId(), exiled, buggy.getId());
        buggy.setSummoningSick(false);
        return buggy;
    }

    @Test
    @DisplayName("Casting the hidden card returns the Buggy before players receive priority")
    void returnsDuringCombatDamageAbilityResolution() {
        GrizzlyBears exiled = new GrizzlyBears();
        addBuggyWithImprint(exiled);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == exiled);
        harness.assertNotOnBattlefield(player1, "Smuggler's Buggy");
        harness.assertInHand(player1, "Smuggler's Buggy");
    }

    @Test
    @DisplayName("A card hidden from a one-card library can be cast after combat damage")
    void castsCardHiddenFromOneCardLibrary() {
        GrizzlyBears exiled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiled));
        Permanent buggy = harness.enterBattlefieldAndReturn(player1, new SmugglersBuggy());
        harness.passBothPriorities();
        buggy.setSummoningSick(false);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiled);
        harness.assertInHand(player1, "Smuggler's Buggy");
    }

    @Test
    @DisplayName("A card selected by hideaway can be cast after combat damage")
    void castsCardSelectedByHideaway() {
        GrizzlyBears exiled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiled, new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        Permanent buggy = harness.enterBattlefieldAndReturn(player1, new SmugglersBuggy());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        buggy.setSummoningSick(false);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiled);
        harness.assertInHand(player1, "Smuggler's Buggy");
    }
}
