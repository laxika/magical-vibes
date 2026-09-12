package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NezumiBladeblesser.class, Spellbook.class, GloriousAnthem.class})
class NezumiBladeblesserTest extends BaseCardTest {

    @Test
    @DisplayName("Has neither keyword without a controlled artifact or enchantment")
    void hasNeitherKeywordWithoutQualifyingPermanent() {
        harness.addToBattlefield(player1, new NezumiBladeblesser());

        assertKeywords(false, false);
    }

    @Test
    @DisplayName("Has deathtouch while its controller controls an artifact")
    void hasDeathtouchWithControlledArtifact() {
        harness.addToBattlefield(player1, new NezumiBladeblesser());
        harness.addToBattlefield(player1, new Spellbook());

        assertKeywords(true, false);
    }

    @Test
    @DisplayName("Has menace while its controller controls an enchantment")
    void hasMenaceWithControlledEnchantment() {
        harness.addToBattlefield(player1, new NezumiBladeblesser());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertKeywords(false, true);
    }

    @Test
    @DisplayName("Has both keywords while its controller controls an artifact and an enchantment")
    void hasBothKeywordsWithBothPermanentTypes() {
        harness.addToBattlefield(player1, new NezumiBladeblesser());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertKeywords(true, true);
    }

    @Test
    @DisplayName("Opponent-controlled artifacts and enchantments do not grant the keywords")
    void opponentPermanentsDoNotCount() {
        harness.addToBattlefield(player1, new NezumiBladeblesser());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new GloriousAnthem());

        assertKeywords(false, false);
    }

    @Test
    @DisplayName("Loses each keyword when the corresponding controlled permanent leaves")
    void losesKeywordsWhenQualifyingPermanentsLeave() {
        harness.addToBattlefield(player1, new NezumiBladeblesser());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertKeywords(true, true);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Spellbook"));
        assertKeywords(false, true);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Glorious Anthem"));
        assertKeywords(false, false);
    }

    private void assertKeywords(boolean deathtouch, boolean menace) {
        Permanent bladeblesser = findPermanent(player1, "Nezumi Bladeblesser");
        assertThat(gqs.hasKeyword(gd, bladeblesser, Keyword.DEATHTOUCH)).isEqualTo(deathtouch);
        assertThat(gqs.hasKeyword(gd, bladeblesser, Keyword.MENACE)).isEqualTo(menace);
    }
}
