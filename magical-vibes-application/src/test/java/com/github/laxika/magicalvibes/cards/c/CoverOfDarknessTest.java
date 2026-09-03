package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoverOfDarkness.class, GrizzlyBears.class, SavannahLions.class})
class CoverOfDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type gives matching creatures fear")
    void grantsFearToCreaturesOfChosenType() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownCat = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CoverOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCat, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.FEAR)).isTrue();
    }
}
