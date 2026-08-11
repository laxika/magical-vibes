package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BitterbloomBearerTest extends BaseCardTest {

    @Test
    @DisplayName("At its controller's upkeep, loses 1 life and creates a flying blue-black Faerie")
    void losesLifeAndCreatesFaerieAtUpkeep() {
        harness.addToBattlefield(player1, new BitterbloomBearer());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(token.getCard().getName()).isEqualTo("Faerie");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FAERIE);
        assertThat(token.getCard().getKeywords()).containsExactly(Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new BitterbloomBearer());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().isToken())).isTrue();
    }
}
