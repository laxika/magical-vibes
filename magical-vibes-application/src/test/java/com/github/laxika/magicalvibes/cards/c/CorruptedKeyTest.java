package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorruptedKey.class, GrizzlyBears.class})
class CorruptedKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Corrupted Key gives your creatures menace and deathtouch")
    void tappedKeyGrantsMenaceAndDeathtouchToYourCreatures() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new CorruptedKey());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        key.tap();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Your creatures lose the granted keywords when Corrupted Key becomes untapped")
    void untappedKeyDoesNotGrantKeywords() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new CorruptedKey());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        key.tap();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();

        key.untap();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isFalse();
    }
}
