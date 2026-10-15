package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeafeningSilence;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shinechaser.class, GoldenEgg.class, DeafeningSilence.class})
class ShinechaserTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 1/1 without an artifact or enchantment")
    void noArtifactOrEnchantment() {
        Permanent shinechaser = addShinechaser();

        assertStats(shinechaser, 1, 1);
    }

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an artifact")
    void artifactBoost() {
        Permanent shinechaser = addShinechaser();
        harness.addToBattlefield(player1, new GoldenEgg());

        assertStats(shinechaser, 2, 2);
    }

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an enchantment")
    void enchantmentBoost() {
        Permanent shinechaser = addShinechaser();
        harness.addToBattlefield(player1, new DeafeningSilence());

        assertStats(shinechaser, 2, 2);
    }

    @Test
    @DisplayName("Gets +2/+2 while its controller controls both an artifact and an enchantment")
    void artifactAndEnchantmentBoost() {
        Permanent shinechaser = addShinechaser();
        harness.addToBattlefield(player1, new GoldenEgg());
        harness.addToBattlefield(player1, new DeafeningSilence());

        assertStats(shinechaser, 3, 3);
    }

    @Test
    @DisplayName("Opponent permanents do not satisfy either condition")
    void opponentPermanentsDoNotCount() {
        Permanent shinechaser = addShinechaser();
        harness.addToBattlefield(player2, new GoldenEgg());
        harness.addToBattlefield(player2, new DeafeningSilence());

        assertStats(shinechaser, 1, 1);
    }

    @Test
    @DisplayName("Loses each bonus when the corresponding permanent leaves")
    void losesBonusesWhenPermanentsLeave() {
        Permanent shinechaser = addShinechaser();
        harness.addToBattlefield(player1, new GoldenEgg());
        harness.addToBattlefield(player1, new DeafeningSilence());

        assertStats(shinechaser, 3, 3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Golden Egg"));
        assertStats(shinechaser, 2, 2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Deafening Silence"));
        assertStats(shinechaser, 1, 1);
    }

    @Test
    @DisplayName("Multiple artifacts and enchantments still grant only one bonus each")
    void multipleQualifyingPermanentsDoNotMultiplyBonuses() {
        Permanent shinechaser = addShinechaser();
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        harness.addToBattlefield(player1, new GoldenEgg());
        Permanent firstEnchantment = harness.addToBattlefieldAndReturn(player1, new DeafeningSilence());
        harness.addToBattlefield(player1, new DeafeningSilence());

        assertStats(shinechaser, 3, 3);

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);
        gd.playerBattlefields.get(player1.getId()).remove(firstEnchantment);

        assertStats(shinechaser, 3, 3);
    }

    @Test
    @DisplayName("Artifacts and enchantments outside the battlefield do not grant bonuses")
    void cardsOutsideBattlefieldDoNotCount() {
        Permanent shinechaser = addShinechaser();
        harness.setHand(player1, List.of(new GoldenEgg(), new DeafeningSilence()));
        harness.setGraveyard(player1, List.of(new GoldenEgg(), new DeafeningSilence()));
        harness.setExile(player1, List.of(new GoldenEgg(), new DeafeningSilence()));

        assertStats(shinechaser, 1, 1);
    }

    @Test
    @DisplayName("Each Shinechaser checks its own controller's permanents")
    void bonusesAreIndependentForOpposingShinechasers() {
        Permanent shinechaser = addShinechaser();
        Permanent opposingShinechaser = harness.addToBattlefieldAndReturn(player2, new Shinechaser());
        harness.addToBattlefield(player1, new GoldenEgg());
        harness.addToBattlefield(player1, new DeafeningSilence());

        assertStats(shinechaser, 3, 3);
        assertStats(opposingShinechaser, 1, 1);

        harness.addToBattlefield(player2, new GoldenEgg());

        assertStats(shinechaser, 3, 3);
        assertStats(opposingShinechaser, 2, 2);
    }

    private Permanent addShinechaser() {
        return harness.addToBattlefieldAndReturn(player1, new Shinechaser());
    }

    private void assertStats(Permanent shinechaser, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, shinechaser)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, shinechaser)).isEqualTo(toughness);
    }
}
