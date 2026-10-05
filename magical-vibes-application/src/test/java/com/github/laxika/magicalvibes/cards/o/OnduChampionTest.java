package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnduChampion.class, ExpeditionEnvoy.class, SnappingGnarlid.class})
class OnduChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives trample to your creatures")
    void ownAllyEntryGrantsTrampleToYourCreatures() {
        Permanent gnarlid = addCreatureReady(player1, new SnappingGnarlid());
        harness.castFromHand(player1, new OnduChampion(), "{2}{R}{R}");
        resolveAllTriggers();

        Permanent champion = findPermanent(player1, "Ondu Champion");
        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, gnarlid, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives trample to all your creatures")
    void anotherAllyEntryGrantsTrampleToYourCreatures() {
        Permanent champion = addCreatureReady(player1, new OnduChampion());
        Permanent gnarlid = addCreatureReady(player1, new SnappingGnarlid());
        harness.castFromHand(player1, new ExpeditionEnvoy(), "{W}");
        resolveAllTriggers();

        Permanent ally = findPermanent(player1, "Expedition Envoy");
        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, gnarlid, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Ondu Champion")
    void nonAllyEntryDoesNotTrigger() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new OnduChampion());
        harness.castFromHand(player1, new SnappingGnarlid(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        harness.castFromHand(player1, new OnduChampion(), "{2}{R}{R}");
        resolveAllTriggers();

        Permanent champion = findPermanent(player1, "Ondu Champion");
        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isFalse();
    }
    @Test
    @DisplayName("An opposing Ally entry does not trigger rally")
    void opposingAllyDoesNotTrigger() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new OnduChampion());
        Permanent opposingAlly = harness.enterBattlefieldAndReturn(player2, new ExpeditionEnvoy());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingAlly, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Rally affects creatures present at resolution, not later arrivals or opponents")
    void recipientsAreDeterminedAtResolution() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        Permanent champion = harness.enterBattlefieldAndReturn(player1, new OnduChampion());
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new SnappingGnarlid());

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isFalse();
        resolveAllTriggers();

        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new SnappingGnarlid());
        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Rally resolves even when Ondu Champion has left the battlefield")
    void rallyResolvesWithoutItsSource() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        Permanent champion = harness.enterBattlefieldAndReturn(player1, new OnduChampion());
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(champion);
        gd.playerGraveyards.get(player1.getId()).add(champion.getCard());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, gnarlid, Keyword.TRAMPLE)).isTrue();
    }
}
