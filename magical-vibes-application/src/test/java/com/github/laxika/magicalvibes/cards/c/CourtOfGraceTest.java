package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.q.QueenMarchesa;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtOfGrace.class, QueenMarchesa.class})
class CourtOfGraceTest extends BaseCardTest {

    @Test
    void entersAndMakesItsControllerTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfGrace());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void createsAnAngelDuringUpkeepWhileItsControllerIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfGrace());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent angel = findPermanent(player1, "Angel");
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void createsASpiritDuringUpkeepWhenAnOpponentIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfGrace());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player2, new QueenMarchesa());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }
}
