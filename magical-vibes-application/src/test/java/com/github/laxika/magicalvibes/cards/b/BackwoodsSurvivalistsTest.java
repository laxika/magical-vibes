package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackwoodsSurvivalistsTest extends BaseCardTest {

    @Test
    @DisplayName("Remains a 4/3 without delirium")
    void noDelirium() {
        Permanent survivalists = addSurvivalists(List.of(new GrizzlyBears(), new Shock(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and trample with four card types in its controller's graveyard")
    void delirium() {
        Permanent survivalists = addSurvivalists(List.of(
                new GrizzlyBears(), new Shock(), new Divination(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus when its controller's graveyard falls below four card types")
    void losesDelirium() {
        Permanent survivalists = addSurvivalists(List.of(
                new GrizzlyBears(), new Shock(), new Divination(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, survivalists)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivalists)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, survivalists, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addSurvivalists(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        return harness.addToBattlefieldAndReturn(player1, new BackwoodsSurvivalists());
    }
}
