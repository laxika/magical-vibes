package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WatchwingScarecrowTest extends BaseCardTest {

    private Permanent scarecrow() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Watchwing Scarecrow"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("No vigilance or flying with no colored creatures")
    void noKeywordsAlone() {
        harness.addToBattlefield(player1, new WatchwingScarecrow());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has vigilance while controlling a white creature")
    void vigilanceWithWhiteCreature() {
        harness.addToBattlefield(player1, new WatchwingScarecrow());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying while controlling a blue creature")
    void flyingWithBlueCreature() {
        harness.addToBattlefield(player1, new WatchwingScarecrow());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Has both keywords with a white and a blue creature")
    void bothKeywordsWithBothColors() {
        harness.addToBattlefield(player1, new WatchwingScarecrow());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's colored creatures don't grant keywords")
    void opponentCreaturesDontCount() {
        harness.addToBattlefield(player1, new WatchwingScarecrow());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        Permanent scarecrow = scarecrow();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }
}
