package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VitoFanaticOfAclazotz.class, GrizzlyBears.class})
class VitoFanaticOfAclazotzTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice resolutions gain life, drain opponents, then create a Vampire Demon")
    void sacrificeResolutionsProgressThroughModes() {
        addCreatureReady(player1, new VitoFanaticOfAclazotz());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        int playerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        sacrifice(first);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(playerLifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);

        sacrifice(second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(playerLifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);

        sacrifice(third);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(playerLifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);

        Permanent token = findPermanent(player1, "Vampire Demon");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
        assertThat(token.getCard().hasKeyword(Keyword.FLYING)).isTrue();
    }

    private void sacrifice(Permanent permanent) {
        harness.inMutationScope(() -> {
            assertThat(gd.playerBattlefields.get(player1.getId()).remove(permanent)).isTrue();
            gd.playerGraveyards.get(player1.getId()).add(permanent.getCard());
            harness.getTriggerCollectionService()
                    .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), permanent.getCard());
        });
        resolveAllTriggers();
    }
}
