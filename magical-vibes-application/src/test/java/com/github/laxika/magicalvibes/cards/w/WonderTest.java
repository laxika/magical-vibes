package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wonder.class, Island.class, GrizzlyBears.class})
class WonderTest extends BaseCardTest {

    @Test
    @DisplayName("A Wonder in the graveyard gives your creatures flying while you control an Island")
    void grantsFlyingFromGraveyardWithIsland() {
        gd.playerGraveyards.get(player1.getId()).add(new Wonder());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wonder's graveyard ability turns off without an Island or after Wonder leaves the graveyard")
    void graveyardAbilityTurnsOffWhenConditionChanges() {
        Wonder wonder = new Wonder();
        gd.playerGraveyards.get(player1.getId()).add(wonder);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();

        Island island = new Island();
        harness.addToBattlefield(player1, island);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getCard() == island);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();

        gd.playerGraveyards.get(player1.getId()).remove(wonder);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }
}
