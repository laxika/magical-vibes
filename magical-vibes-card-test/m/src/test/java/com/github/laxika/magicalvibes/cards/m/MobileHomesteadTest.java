package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MobileHomestead.class, BrightfieldGlider.class, Forest.class, GrizzlyBears.class})
class MobileHomesteadTest extends BaseCardTest {

    @Test
    @DisplayName("Has haste while its controller controls a Mount")
    void hasHasteWhileControllingMount() {
        Permanent homestead = harness.addToBattlefieldAndReturn(player1, new MobileHomestead());
        assertThat(gqs.hasKeyword(gd, homestead, Keyword.HASTE)).isFalse();

        harness.addToBattlefield(player1, new BrightfieldGlider());

        assertThat(gqs.hasKeyword(gd, homestead, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not get haste from an opponent's Mount")
    void doesNotGetHasteFromOpponentsMount() {
        Permanent homestead = harness.addToBattlefieldAndReturn(player1, new MobileHomestead());
        harness.addToBattlefield(player2, new BrightfieldGlider());

        assertThat(gqs.hasKeyword(gd, homestead, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Crew 2 animates Mobile Homestead and taps the creatures used to crew it")
    void crewAnimatesAndTapsCrew() {
        Permanent homestead = harness.addToBattlefieldAndReturn(player1, new MobileHomestead());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, homestead)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking lets its controller put a top land onto the battlefield tapped")
    void attackPutsTopLandOntoBattlefieldTapped() {
        Permanent homestead = addCreatureReady(player1, new MobileHomestead());
        addCreatureReady(player1, new GrizzlyBears());
        crew(homestead);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Forest")).singleElement().satisfies(forest ->
                assertThat(forest.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the top land on the library")
    void decliningAttackTriggerLeavesLandOnTop() {
        Permanent homestead = addCreatureReady(player1, new MobileHomestead());
        addCreatureReady(player1, new GrizzlyBears());
        crew(homestead);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    @Test
    @DisplayName("Loses haste when its controller no longer controls a Mount")
    void losesHasteWhenMountLeaves() {
        Permanent homestead = harness.addToBattlefieldAndReturn(player1, new MobileHomestead());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        assertThat(gqs.hasKeyword(gd, homestead, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(mount);

        assertThat(gqs.hasKeyword(gd, homestead, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay the crew cost")
    void summoningSickCreatureCanCrew() {
        Permanent homestead = harness.addToBattlefieldAndReturn(player1, new MobileHomestead());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        crew(homestead);

        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking lets only its controller see a nonland left on top")
    void attackShowsNonlandPrivatelyAndLeavesItOnTop() {
        Permanent homestead = addCreatureReady(player1, new MobileHomestead());
        addCreatureReady(player1, new GrizzlyBears());
        crew(homestead);
        BrightfieldGlider topCard = new BrightfieldGlider();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        harness.clearMessages();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.publishState();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(findPermanents(player1, "Brightfield Glider")).isEmpty();
        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("Brightfield Glider"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Brightfield Glider"));
    }

    @Test
    @DisplayName("Attacking with an empty library requires no choice and does not lose the game")
    void attackWithEmptyLibraryDoesNothing() {
        Permanent homestead = addCreatureReady(player1, new MobileHomestead());
        addCreatureReady(player1, new GrizzlyBears());
        crew(homestead);
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void crew(Permanent homestead) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, homestead)).isTrue();
    }
}
