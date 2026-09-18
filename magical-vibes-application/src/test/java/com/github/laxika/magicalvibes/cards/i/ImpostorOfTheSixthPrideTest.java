package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpostorOfTheSixthPride.class, FieldMarshal.class})
class ImpostorOfTheSixthPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling makes Impostor of the Sixth Pride a Soldier")
    void changelingMakesItASoldier() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new ImpostorOfTheSixthPride());

        GameData gd = harness.getGameData();
        Permanent impostor = findPermanent(player1, "Impostor of the Sixth Pride");

        assertThat(gqs.getEffectivePower(gd, impostor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, impostor)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, impostor, Keyword.FIRST_STRIKE)).isTrue();
    }
}
