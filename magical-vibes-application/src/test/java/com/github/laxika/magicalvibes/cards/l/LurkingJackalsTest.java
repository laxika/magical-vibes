package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LurkingJackals.class)
class LurkingJackalsTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 3/2 Jackal creature when an opponent has 10 or less life")
    void becomesCreatureWhenOpponentHasTenOrLessLife() {
        Permanent jackals = harness.addToBattlefieldAndReturn(player1, new LurkingJackals());

        harness.setLife(player2, 10);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jackals)).isTrue();
        assertThat(gqs.isEnchantment(gd, jackals)).isFalse();
        assertThat(jackals.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(jackals.getCard().getSubtypes()).containsExactly(CardSubtype.JACKAL);
        assertThat(jackals.getCard().getPower()).isEqualTo(3);
        assertThat(jackals.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform above the life threshold and does not revert after transforming")
    void thresholdAndPermanentTransformation() {
        Permanent jackals = harness.addToBattlefieldAndReturn(player1, new LurkingJackals());

        harness.setLife(player2, 11);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, jackals)).isFalse();
        assertThat(gqs.isEnchantment(gd, jackals)).isTrue();

        harness.setLife(player2, 10);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setLife(player2, 11);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jackals)).isTrue();
        assertThat(gqs.isEnchantment(gd, jackals)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when only its controller has 10 or less life")
    void doesNotTriggerForControllersLowLife() {
        Permanent jackals = harness.addToBattlefieldAndReturn(player1, new LurkingJackals());

        harness.setLife(player1, 10);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jackals)).isFalse();
        assertThat(gqs.isEnchantment(gd, jackals)).isTrue();
    }

    @Test
    @DisplayName("Still transforms if the opponent rises above 10 life after the trigger")
    void transformsAfterOpponentRisesAboveThresholdBeforeResolution() {
        Permanent jackals = harness.addToBattlefieldAndReturn(player1, new LurkingJackals());
        harness.setLife(player2, 10);
        harness.runStateBasedActions();

        harness.setLife(player2, 11);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jackals)).isTrue();
        assertThat(gqs.isEnchantment(gd, jackals)).isFalse();
    }

    @Test
    @CardUsed(ImprisonedInTheMoon.class)
    @DisplayName("Does not transform if it is no longer an enchantment when the trigger resolves")
    void doesNotTransformWhenNoLongerEnchantmentAtResolution() {
        Permanent jackals = harness.addToBattlefieldAndReturn(player1, new LurkingJackals());
        harness.setLife(player2, 10);
        harness.runStateBasedActions();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(jackals.getId());
        assertThat(gqs.isEnchantment(gd, jackals)).isFalse();

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, jackals)).isFalse();
        assertThat(gqs.isEnchantment(gd, jackals)).isTrue();
    }
}
