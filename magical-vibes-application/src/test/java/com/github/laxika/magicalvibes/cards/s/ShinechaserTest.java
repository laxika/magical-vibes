package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shinechaser.class, Spellbook.class, Crawlspace.class})
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
        harness.addToBattlefield(player1, new Spellbook());

        assertStats(shinechaser, 2, 2);
    }

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an enchantment")
    void enchantmentBoost() {
        Permanent shinechaser = addShinechaser();
        harness.addToBattlefield(player1, new Crawlspace());

        assertStats(shinechaser, 2, 2);
    }

    @Test
    @DisplayName("Gets +2/+2 while its controller controls both an artifact and an enchantment")
    void artifactAndEnchantmentBoost() {
        Permanent shinechaser = addShinechaser();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Crawlspace());

        assertStats(shinechaser, 3, 3);
    }

    @Test
    @DisplayName("Opponent permanents do not satisfy either condition")
    void opponentPermanentsDoNotCount() {
        Permanent shinechaser = addShinechaser();
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Crawlspace());

        assertStats(shinechaser, 1, 1);
    }

    @Test
    @DisplayName("Loses each bonus when the corresponding permanent leaves")
    void losesBonusesWhenPermanentsLeave() {
        Permanent shinechaser = addShinechaser();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Crawlspace());

        assertStats(shinechaser, 3, 3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Spellbook"));
        assertStats(shinechaser, 2, 2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Crawlspace"));
        assertStats(shinechaser, 1, 1);
    }

    private Permanent addShinechaser() {
        return harness.addToBattlefieldAndReturn(player1, new Shinechaser());
    }

    private void assertStats(Permanent shinechaser, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, shinechaser)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, shinechaser)).isEqualTo(toughness);
    }
}
