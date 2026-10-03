package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalothPrime.class, Forest.class})
class BalothPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Baloth Prime enters tapped with six stun counters")
    void entersTappedWithStunCounters() {
        harness.setHand(player1, List.of(new BalothPrime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent baloth = findPermanent(player1, "Baloth Prime");
        assertThat(baloth.isTapped()).isTrue();
        assertThat(baloth.getCounterCount(CounterType.STUN)).isEqualTo(6);
    }

    @Test
    @DisplayName("Sacrificing a land creates a tapped Beast, untaps Baloth Prime, and gains life")
    void sacrificingLandCreatesBeastUntapsAndGainsLife() {
        Permanent baloth = addCreatureReady(player1, new BalothPrime());
        baloth.tap();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(baloth),
                0, null, null);
        harness.handlePermanentChosen(player1, forest.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(baloth.isTapped()).isFalse();

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().isToken()).isTrue();
        assertThat(beast.isTapped()).isTrue();
        assertThat(beast.getCard().getColors()).containsExactly(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(beast.getEffectivePower()).isEqualTo(4);
        assertThat(beast.getEffectiveToughness()).isEqualTo(4);
    }
}
