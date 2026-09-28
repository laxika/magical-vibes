package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeafLeapGuide.class, GrizzlyBears.class})
class LeafLeapGuideTest extends BaseCardTest {

    @Test
    void perpetuallyBoostsTheGuideAndEnteringCreature() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new LeafLeapGuide());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guide)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(3);
    }

    @Test
    void perpetualBoostSurvivesReturningCreatureToHand() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new LeafLeapGuide());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        guide.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, entering.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");

        Card enteringCard = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        harness.setHand(player1, List.of(enteringCard));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).singleElement()
                .satisfies(reentered -> {
                    assertThat(gqs.getEffectivePower(gd, reentered)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, reentered)).isEqualTo(4);
                });
    }

    @Test
    void cannotReturnTheGuideItself() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new LeafLeapGuide());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guide.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
