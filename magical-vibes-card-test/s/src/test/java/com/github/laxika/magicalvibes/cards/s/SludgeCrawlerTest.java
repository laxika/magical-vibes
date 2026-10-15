package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SludgeCrawler.class})
class SludgeCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card of the damaged player's library")
    void combatDamageExilesTopCard() {
        Permanent crawler = addCreatureReady(player1, new SludgeCrawler());
        crawler.setAttacking(true);
        SludgeCrawler topCard = new SludgeCrawler();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
    }

    @Test
    @DisplayName("The generic activated ability repeatedly gives Sludge Crawler +1/+1")
    void activatedAbilityPumps() {
        Permanent crawler = addCreatureReady(player1, new SludgeCrawler());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crawler.getEffectivePower()).isEqualTo(3);
        assertThat(crawler.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability's boost wears off at end of turn")
    void activatedAbilityBoostWearsOff() {
        Permanent crawler = addCreatureReady(player1, new SludgeCrawler());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crawler.getEffectivePower()).isEqualTo(1);
        assertThat(crawler.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ingest waits for resolution and exiles exactly one card even after pumping")
    void ingestUsesTheStackAndExilesOnlyOneCard() {
        Permanent crawler = addCreatureReady(player1, new SludgeCrawler());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        crawler.setAttacking(true);
        SludgeCrawler topCard = new SludgeCrawler();
        SludgeCrawler nextCard = new SludgeCrawler();
        SludgeCrawler ownCard = new SludgeCrawler();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setLibrary(player1, List.of(ownCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard);

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(topCard.getId()).ownerId()).isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
    }

    @Test
    @DisplayName("Ingest resolves harmlessly when the damaged player's library is empty")
    void ingestWithEmptyLibrary() {
        Permanent crawler = addCreatureReady(player1, new SludgeCrawler());
        crawler.setAttacking(true);
        harness.setLibrary(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Sludge Crawler can pump using colored mana")
    void tappedSummoningSickCrawlerCanActivate() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new SludgeCrawler());
        crawler.setSummoningSick(true);
        crawler.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crawler.getEffectivePower()).isEqualTo(2);
        assertThat(crawler.getEffectiveToughness()).isEqualTo(2);
        assertThat(crawler.isTapped()).isTrue();
    }
}
