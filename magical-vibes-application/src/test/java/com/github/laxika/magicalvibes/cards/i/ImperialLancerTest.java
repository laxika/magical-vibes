package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LegionConquistador;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImperialLancer.class, RaptorHatchling.class, LegionConquistador.class})
class ImperialLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Has double strike when controller controls a Dinosaur")
    void hasDoubleStrikeWithDinosaur() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ImperialLancer());
        harness.addToBattlefield(player1, new RaptorHatchling());

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("No double strike without a Dinosaur")
    void noDoubleStrikeWithoutDinosaur() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ImperialLancer());

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("No double strike with a non-Dinosaur creature")
    void noDoubleStrikeWithNonDinosaurCreature() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ImperialLancer());
        harness.addToBattlefield(player1, new LegionConquistador());

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses double strike when Dinosaur leaves the battlefield")
    void losesDoubleStrikeWhenDinosaurLeaves() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ImperialLancer());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new RaptorHatchling());

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(dinosaur);

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Dinosaur does not grant double strike")
    void opponentDinosaurDoesNotCount() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ImperialLancer());
        harness.addToBattlefield(player2, new RaptorHatchling());

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gains double strike immediately when a Dinosaur enters")
    void gainsDoubleStrikeWhenDinosaurEnters() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ImperialLancer());
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new RaptorHatchling());

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Retains double strike while at least one Dinosaur remains")
    void retainsDoubleStrikeWithRemainingDinosaur() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ImperialLancer());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RaptorHatchling());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RaptorHatchling());

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A Dinosaur in hand or graveyard does not grant double strike")
    void dinosaurOutsideBattlefieldDoesNotCount() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new ImperialLancer());
        harness.setHand(player1, List.of(new RaptorHatchling()));
        harness.setGraveyard(player1, List.of(new RaptorHatchling()));

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike is granted only to Imperial Lancer")
    void doesNotGrantDoubleStrikeToOtherCreatures() {
        harness.addToBattlefield(player1, new ImperialLancer());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new RaptorHatchling());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());

        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Deals damage in both combat damage steps with a Dinosaur")
    void dealsDoubleStrikeCombatDamage() {
        addCreatureReady(player1, new ImperialLancer());
        harness.addToBattlefield(player1, new RaptorHatchling());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals damage only once without a Dinosaur")
    void dealsNormalCombatDamageWithoutDinosaur() {
        addCreatureReady(player1, new ImperialLancer());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
    }

}
