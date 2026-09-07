package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Filth.class, Swamp.class, GrizzlyBears.class})
class FilthTest extends BaseCardTest {

    @Test
    @DisplayName("A Filth in the graveyard gives your creatures swampwalk while you control a Swamp")
    void grantsSwampwalkFromGraveyardWithSwamp() {
        gd.playerGraveyards.get(player1.getId()).add(new Filth());
        harness.addToBattlefield(player1, new Swamp());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("Filth's graveyard ability turns off without a Swamp or after Filth leaves the graveyard")
    void graveyardAbilityTurnsOffWhenConditionChanges() {
        Filth filth = new Filth();
        gd.playerGraveyards.get(player1.getId()).add(filth);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isFalse();

        Swamp swamp = new Swamp();
        harness.addToBattlefield(player1, swamp);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getCard() == swamp);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isFalse();

        gd.playerGraveyards.get(player1.getId()).remove(filth);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isFalse();
    }
}
