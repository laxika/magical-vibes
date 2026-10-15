package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.SteelOfTheGodhead;
import com.github.laxika.magicalvibes.cards.z.ZealousGuardian;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatchwingScarecrow.class, EliteVanguard.class, FugitiveWizard.class,
        ZealousGuardian.class, SteelOfTheGodhead.class})
class WatchwingScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("No vigilance or flying with no colored creatures")
    void noKeywordsAlone() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WatchwingScarecrow());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has vigilance while controlling a white creature")
    void vigilanceWithWhiteCreature() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WatchwingScarecrow());
        harness.addToBattlefield(player1, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying while controlling a blue creature")
    void flyingWithBlueCreature() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WatchwingScarecrow());
        harness.addToBattlefield(player1, new FugitiveWizard());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Has both keywords with a white and a blue creature")
    void bothKeywordsWithBothColors() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WatchwingScarecrow());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new FugitiveWizard());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's colored creatures don't grant keywords")
    void opponentCreaturesDontCount() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WatchwingScarecrow());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("One white-blue creature grants both keywords, which disappear when it leaves")
    void multicoloredCreatureGrantsBothKeywordsOnlyWhileControlled() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WatchwingScarecrow());
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();

        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new ZealousGuardian());
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(guardian);
        gd.playerGraveyards.get(player1.getId()).add(guardian.getCard());
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A white-blue noncreature permanent does not grant either keyword")
    void coloredNoncreatureDoesNotCount() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new WatchwingScarecrow());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SteelOfTheGodhead());
        aura.setAttachedTo(scarecrow.getId());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.FLYING)).isFalse();
    }
}
