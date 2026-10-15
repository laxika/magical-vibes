package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.t.TheHive;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathBegetsLife.class, Forest.class, GrizzlyBears.class, RuleOfLaw.class, TrollAscetic.class, TheHive.class})
class DeathBegetsLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and enchantments and draws for each permanent destroyed")
    void destroysCreaturesAndEnchantmentsAndDrawsForEachDestroyed() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.addToBattlefield(player1, new Forest());

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castFromHand(player1, new DeathBegetsLife(), "{5}{B}{G}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Does not count indestructible creatures among destroyed permanents")
    void doesNotCountIndestructibleCreatures() {
        Card indestructibleBears = new GrizzlyBears();
        indestructibleBears.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        harness.addToBattlefield(player1, indestructibleBears);
        harness.addToBattlefield(player2, new RuleOfLaw());

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castFromHand(player1, new DeathBegetsLife(), "{5}{B}{G}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws no cards when no creatures or enchantments are destroyed")
    void drawsNothingWhenNoPermanentsAreDestroyed() {
        harness.addToBattlefield(player1, new Forest());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new DeathBegetsLife(), "{5}{B}{G}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Counts destroyed creature tokens and leaves noncreature artifacts intact")
    void countsDestroyedTokens() {
        harness.addToBattlefield(player1, new TheHive());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wasp");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new DeathBegetsLife(), "{5}{B}{G}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wasp");
        harness.assertOnBattlefield(player1, "The Hive");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Regeneration prevents destruction and does not contribute to the draw count")
    void doesNotCountRegeneratedCreatures() {
        harness.addToBattlefield(player1, new TrollAscetic());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new DeathBegetsLife(), "{5}{B}{G}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Troll Ascetic");
        harness.assertNotInGraveyard(player1, "Troll Ascetic");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }
}
