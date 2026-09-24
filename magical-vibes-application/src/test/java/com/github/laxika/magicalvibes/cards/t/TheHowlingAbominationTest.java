package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHowlingAbomination.class, Shock.class})
class TheHowlingAbominationTest extends BaseCardTest {

    @Test
    void becomesBiggerAndDamagesEachOpponentWhenTargetedBySpell() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new TheHowlingAbomination());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, abomination.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, abomination)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, abomination)).isEqualTo(7);
    }

    @Test
    void gainsTrampleAfterControllerCastsThreeSpells() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new TheHowlingAbomination());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, abomination, Keyword.TRAMPLE)).isFalse();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, abomination, Keyword.TRAMPLE)).isTrue();
    }
}
