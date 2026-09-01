package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
        Permanent spellbook = addPermanentReady(player1, new Spellbook());

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
        Permanent spellbook = addPermanentReady(player1, new Spellbook());

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

    private Permanent addPermanentReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
