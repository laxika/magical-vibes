package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CanyonCrawler;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AppaSteadfastGuardian.class, CanyonCrawler.class, Island.class})
class AppaSteadfastGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Airbends any number of other nonland permanents you control")
    void airbendsAnyNumberOfOtherNonlandPermanentsYouControl() {
        Permanent firstCrawler = harness.addToBattlefieldAndReturn(player1, new CanyonCrawler());
        Permanent secondCrawler = harness.addToBattlefieldAndReturn(player1, new CanyonCrawler());

        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();
        harness.castCreature(player1, 0, List.of(firstCrawler.getId(), secondCrawler.getId()));
        resolveAllTriggers();

        assertThat(gd.findExiledCard(firstCrawler.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(secondCrawler.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Casting an airbent spell from exile creates an Ally token")
    void castingAirbentSpellFromExileCreatesAllyToken() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new CanyonCrawler());

        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();
        harness.castCreature(player1, 0, List.of(crawler.getId()));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, crawler.getOriginalCard().getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ally")).hasSize(1);
        assertThat(findPermanent(player1, "Ally").getCard().getSubtypes())
                .containsExactly(CardSubtype.ALLY);
    }

    @Test
    @DisplayName("Airbend cannot target a land")
    void airbendCannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    void canChooseZeroTargets() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new CanyonCrawler());
        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();

        harness.castCreature(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Appa, Steadfast Guardian");
        assertThat(gd.findExiledCard(crawler.getOriginalCard().getId())).isNull();
        harness.assertOnBattlefield(player1, "Canyon Crawler");
        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    @Test
    void cannotTargetOpponentsPermanent() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player2, new CanyonCrawler());
        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(crawler.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAirbendNoncreatureArtifactToken() {
        harness.enterBattlefieldAndReturn(player1, new CanyonCrawler());
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");
        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();

        harness.castCreature(player1, 0, List.of(food.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertOnBattlefield(player1, "Canyon Crawler");
        harness.assertOnBattlefield(player1, "Appa, Steadfast Guardian");
        assertThat(gd.findExiledCard(food.getOriginalCard().getId())).isNull();
        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    @Test
    void castingFromHandDoesNotCreateAlly() {
        harness.addToBattlefield(player1, new AppaSteadfastGuardian());
        harness.setHand(player1, List.of(new CanyonCrawler()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Canyon Crawler");
        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    @Test
    void allyIsCreatedBeforeExiledSpellResolves() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new CanyonCrawler());
        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();
        harness.castCreature(player1, 0, List.of(crawler.getId()));
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, crawler.getOriginalCard().getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Canyon Crawler");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Canyon Crawler");
        assertThat(findPermanents(player1, "Ally")).hasSize(1);
    }

    @Test
    void opponentsExileCastDoesNotTriggerYourAppa() {
        harness.addToBattlefield(player1, new AppaSteadfastGuardian());
        Permanent crawler = harness.addToBattlefieldAndReturn(player2, new CanyonCrawler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AppaSteadfastGuardian()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0, List.of(crawler.getId()));
        resolveAllTriggers();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromExile(player2, crawler.getOriginalCard().getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ally")).isEmpty();
        assertThat(findPermanents(player2, "Ally")).hasSize(1);
    }

    @Test
    void canCastAppaDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();

        harness.castCreature(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Appa, Steadfast Guardian");
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new AppaSteadfastGuardian());
        addCreatureReady(player2, new CanyonCrawler());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void remainingLegalTargetIsAirbentWhenAnotherTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CanyonCrawler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CanyonCrawler());
        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();
        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, first));

        resolveAllTriggers();

        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNull();
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNotNull();
        harness.assertInGraveyard(player1, "Canyon Crawler");
    }

    @Test
    void airbendPermissionRemainsAfterAppaLeaves() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new CanyonCrawler());
        harness.setHand(player1, List.of(new AppaSteadfastGuardian()));
        addAppaMana();
        harness.castCreature(player1, 0, List.of(crawler.getId()));
        resolveAllTriggers();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, findPermanent(player1, "Appa, Steadfast Guardian")));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, crawler.getOriginalCard().getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Canyon Crawler");
        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    private void addAppaMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
