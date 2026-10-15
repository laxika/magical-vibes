package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SoulReap;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thunderblust.class, SoulReap.class})
class ThunderblustTest extends BaseCardTest {

    @Test
    @DisplayName("No trample without a -1/-1 counter")
    void noTrampleWithoutCounter() {
        Permanent thunderblust = harness.addToBattlefieldAndReturn(player1, new Thunderblust());
        assertThat(gqs.hasKeyword(gd, thunderblust, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Has trample while it has a -1/-1 counter")
    void hasTrampleWithCounter() {
        Permanent thunderblust = harness.addToBattlefieldAndReturn(player1, new Thunderblust());
        thunderblust.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, thunderblust, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Loses trample when the -1/-1 counter is removed")
    void losesTrampleWhenCounterRemoved() {
        Permanent thunderblust = harness.addToBattlefieldAndReturn(player1, new Thunderblust());
        thunderblust.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, thunderblust, Keyword.TRAMPLE)).isTrue();

        thunderblust.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, thunderblust, Keyword.TRAMPLE)).isFalse();
    }
    @Test
    void canAttackImmediatelyWithHaste() {
        Permanent thunderblust = harness.addToBattlefieldAndReturn(player1, new Thunderblust());
        thunderblust.setSummoningSick(true);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void persistReturnsWithCounterAndTrampleButOnlyOnce() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Thunderblust());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SoulReap(), new SoulReap()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, original.getId());

        harness.assertNotOnBattlefield(player1, "Thunderblust");
        harness.assertInGraveyard(player1, "Thunderblust");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Thunderblust");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card instanceof Thunderblust);

        harness.castAndResolveSorcery(player1, 0, returned.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Thunderblust");
        harness.assertInGraveyard(player1, "Thunderblust");
    }

    @Test
    void doesNotPersistWhenItAlreadyHasMinusOneCounter() {
        Permanent thunderblust = harness.addToBattlefieldAndReturn(player1, new Thunderblust());
        thunderblust.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SoulReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, thunderblust.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Thunderblust");
        harness.assertInGraveyard(player1, "Thunderblust");
    }
}
