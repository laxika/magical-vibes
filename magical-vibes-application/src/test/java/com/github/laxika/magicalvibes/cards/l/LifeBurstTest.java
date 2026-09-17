package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifeBurst.class, AvenFlock.class})
class LifeBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 4 life plus 4 for each Life Burst in all graveyards")
    void gainsLifeForCopiesInAllGraveyards() {
        harness.setLife(player2, 10);
        harness.setGraveyard(player1, List.of(new LifeBurst()));
        harness.setGraveyard(player2, List.of(new LifeBurst(), new AvenFlock()));
        harness.setHand(player1, List.of(new LifeBurst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The spell itself is not counted until after resolution")
    void doesNotCountSpellOnStack() {
        harness.setLife(player2, 10);
        harness.setGraveyard(player1, List.of(new LifeBurst()));
        harness.setHand(player1, List.of(new LifeBurst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Life Burst");
    }

    @Test
    @DisplayName("Gains only the base amount when no Life Burst is in either graveyard")
    void gainsOnlyBaseAmountWithNoCopiesInGraveyards() {
        harness.setLife(player2, 10);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new LifeBurst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.setHand(player1, List.of(new LifeBurst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
