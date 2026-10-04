package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeFirebrand.class, ChandraNalaar.class, GreenwoodSentinel.class})
class RenegadeFirebrandTest extends BaseCardTest {

    @Test
    void getsPowerAndFirstStrikeWithChandraPlaneswalker() {
        Permanent firebrand = addCreatureReady(player1, new RenegadeFirebrand());
        int basePower = gqs.getEffectivePower(gd, firebrand);
        int baseToughness = gqs.getEffectiveToughness(gd, firebrand);

        harness.addToBattlefield(player1, new ChandraNalaar());

        assertThat(gqs.getEffectivePower(gd, firebrand)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, firebrand)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, firebrand, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void doesNotGetBonusWithoutChandraPlaneswalker() {
        Permanent firebrand = addCreatureReady(player1, new RenegadeFirebrand());
        int basePower = gqs.getEffectivePower(gd, firebrand);

        assertThat(gqs.getEffectivePower(gd, firebrand)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, firebrand, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void opponentsChandraDoesNotCount() {
        Permanent firebrand = addCreatureReady(player1, new RenegadeFirebrand());
        int basePower = gqs.getEffectivePower(gd, firebrand);

        harness.addToBattlefield(player2, new ChandraNalaar());

        assertThat(gqs.getEffectivePower(gd, firebrand)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, firebrand, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void chandraSubtypeOnNonPlaneswalkerDoesNotCount() {
        Permanent firebrand = addCreatureReady(player1, new RenegadeFirebrand());
        int basePower = gqs.getEffectivePower(gd, firebrand);
        Card chandraCreature = new GreenwoodSentinel();
        chandraCreature.setSubtypes(List.of(CardSubtype.CHANDRA));

        harness.addToBattlefield(player1, chandraCreature);

        assertThat(gqs.getEffectivePower(gd, firebrand)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, firebrand, Keyword.FIRST_STRIKE)).isFalse();
    }
}
