package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianAwakening.class, CrawlingChorus.class, GrizzlyBears.class})
class PhyrexianAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an Incubator token with four +1/+1 counters")
    void entersWithIncubatorToken() {
        castAwakening();
        resolveAwakening();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives vigilance to Phyrexian creatures you control only")
    void givesVigilanceToOwnPhyrexians() {
        Permanent phyrexian = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        Permanent nonPhyrexian = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PhyrexianAwakening());

        assertThat(gqs.hasKeyword(gd, phyrexian, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonPhyrexian, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The Incubator transforms into a vigilant Phyrexian")
    void incubatorTransformsIntoVigilantPhyrexian() {
        castAwakening();
        resolveAwakening();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.hasKeyword(gd, incubator, Keyword.VIGILANCE)).isTrue();
    }

    private void castAwakening() {
        harness.setHand(player1, List.of(new PhyrexianAwakening()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
    }

    private void resolveAwakening() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
