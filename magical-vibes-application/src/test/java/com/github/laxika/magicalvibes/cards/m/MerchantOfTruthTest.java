package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerchantOfTruth.class, GrizzlyBears.class, Shock.class})
class MerchantOfTruthTest extends BaseCardTest {

    @Test
    void investigatesWhenANontokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void cluesYouControlHaveExalted() {
        harness.addToBattlefield(player1, new MerchantOfTruth());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        assertThat(gqs.hasKeyword(gd, clue, Keyword.EXALTED)).isTrue();

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }
}
