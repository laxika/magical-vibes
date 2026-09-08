package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SparkhunterMasticoreTest extends BaseCardTest {

    @Test
    void requiresDiscardingACardToCast() {
        harness.setHand(player1, List.of(new SparkhunterMasticore(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sparkhunter Masticore");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void cannotCastWithoutAnotherCardToDiscard() {
        harness.setHand(player1, List.of(new SparkhunterMasticore()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void damageAbilityDealsDamageToTargetPlaneswalker() {
        addCreatureReady(player1, new SparkhunterMasticore());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void damageAbilityCannotTargetAPlayer() {
        addCreatureReady(player1, new SparkhunterMasticore());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void protectionFromPlaneswalkersPreventsTheirAbilitiesTargetingIt() {
        Permanent sparkhunter = addCreatureReady(player1, new SparkhunterMasticore());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, 2, sparkhunter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void indestructibleAbilityLastsUntilEndOfTurn() {
        Permanent sparkhunter = addCreatureReady(player1, new SparkhunterMasticore());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sparkhunter, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sparkhunter, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
