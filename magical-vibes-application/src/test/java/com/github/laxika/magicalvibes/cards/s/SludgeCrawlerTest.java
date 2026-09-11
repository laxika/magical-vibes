package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SludgeCrawler.class, GrizzlyBears.class})
class SludgeCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card of the damaged player's library")
    void combatDamageExilesTopCard() {
        Permanent crawler = addCreatureReady(player1, new SludgeCrawler());
        crawler.setAttacking(true);
        GrizzlyBears topCard = new GrizzlyBears();
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
}
