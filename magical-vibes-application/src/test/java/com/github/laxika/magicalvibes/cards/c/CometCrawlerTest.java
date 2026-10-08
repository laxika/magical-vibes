package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CometCrawler.class, GrizzlyBears.class, Spellbook.class})
class CometCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers another creature or artifact, but not Comet Crawler")
    void attackingOffersAnotherCreatureOrArtifact() {
        Permanent crawler = addCreatureReady(player1, new CometCrawler());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(bears.getId(), spellbook.getId());
        assertThat(choice.validIds()).doesNotContain(crawler.getId());
    }

    @Test
    @DisplayName("Sacrificing another creature gives Comet Crawler +2/+0")
    void sacrificingCreatureBoosts() {
        Permanent crawler = addCreatureReady(player1, new CometCrawler());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        attackAndAcceptMay();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Sacrificing an artifact gives Comet Crawler +2/+0")
    void sacrificingArtifactBoosts() {
        Permanent crawler = addCreatureReady(player1, new CometCrawler());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        attackAndAcceptMay();
        harness.handlePermanentChosen(player1, spellbook.getId());

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spellbook.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does not boost Comet Crawler")
    void decliningSacrificeDoesNothing() {
        Permanent crawler = addCreatureReady(player1, new CometCrawler());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent crawler = addCreatureReady(player1, new CometCrawler());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        attackAndAcceptMay();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
    }

    private void attackAndAcceptMay() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Accepting with no other creature or artifact does not boost the crawler")
    void noEligibleSacrificeDoesNotBoost() {
        Permanent crawler = addCreatureReady(player1, new CometCrawler());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            attackAndAcceptMay();

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(2);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(crawler);
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        });
    }

    @Test
    @DisplayName("A returned crawler is eligible for its old attack trigger's sacrifice")
    void returnedCrawlerIsAnotherCreatureForOldTrigger() {
        Permanent crawler = addCreatureReady(player1, new CometCrawler());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        Permanent returned = returnCrawlerAsNewPermanent(crawler);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), returned.getId());
    }

    @Test
    @DisplayName("The old attack trigger does not boost a crawler that left and returned")
    void oldTriggerDoesNotBoostReturnedCrawler() {
        Permanent crawler = addCreatureReady(player1, new CometCrawler());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        Permanent returned = returnCrawlerAsNewPermanent(crawler);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lifelink gains life equal to the boosted combat damage")
    void boostedCombatDamageGainsFourLife() {
        addCreatureReady(player1, new CometCrawler());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        attackAndAcceptMay();
        harness.handlePermanentChosen(player1, spellbook.getId());
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    private Permanent returnCrawlerAsNewPermanent(Permanent crawler) {
        // Model a leave-and-return interaction while the attack trigger is pending.
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, crawler);
        gd.playerGraveyards.get(player1.getId()).remove(crawler.getCard());
        return harness.enterBattlefieldAndReturn(player1, crawler.getCard());
    }
}
