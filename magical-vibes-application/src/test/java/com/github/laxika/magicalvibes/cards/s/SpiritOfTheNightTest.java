package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpiritOfTheNightTest extends BaseCardTest {

    private Permanent addSpirit() {
        Permanent spirit = addCreatureReady(player1, new SpiritOfTheNight());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return spirit;
    }

    @Test
    @DisplayName("Has protection from black but not from other colors")
    void hasProtectionFromBlack() {
        Permanent spirit = addSpirit();

        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Has first strike only while attacking")
    void firstStrikeOnlyWhileAttacking() {
        Permanent spirit = addSpirit();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isFalse();

        spirit.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isTrue();

        spirit.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isFalse();
    }
}
