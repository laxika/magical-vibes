package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.s.Snapback;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Conflagrate.class, AshcoatBear.class, IvoryMask.class, Snapback.class})
class ConflagrateTest extends BaseCardTest {

    @Test
    void normalCastDividesXDamageAmongAnyTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Conflagrate()));
        harness.addMana(player1, ManaColor.RED, 7);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorceryForX(player1, 0, 3, Map.of(bear.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ashcoat Bear");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void flashbackRequiresAndPaysDiscardXCards() {
        harness.setGraveyard(player1, List.of(new Conflagrate()));
        harness.setHand(player1, List.of(new AshcoatBear(), new AshcoatBear(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFlashbackForXWithDiscards(player1, 0, 3, Map.of(player2.getId(), 3), List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Ashcoat Bear", "Ashcoat Bear", "Ashcoat Bear");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Conflagrate"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void flashbackIsRejectedWhenTheHandCannotCoverXDiscards() {
        harness.setGraveyard(player1, List.of(new Conflagrate()));
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashbackForXWithDiscards(
                player1, 0, 2, Map.of(player2.getId(), 2), List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Conflagrate");
    }

    @Test
    void zeroXMayBeCastWithNoTargets() {
        harness.setHand(player1, List.of(new Conflagrate()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorceryForX(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertInGraveyard(player1, "Conflagrate");
    }

    @Test
    void damageAssignmentsMustSumToX() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Conflagrate()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castSorceryForX(
                player1, 0, 3, Map.of(bear.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroXFlashbackRequiresNoDiscardsOrTargets() {
        harness.setGraveyard(player1, List.of(new Conflagrate()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFlashbackForXWithDiscards(player1, 0, 0, Map.of(), List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Conflagrate");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Conflagrate"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void everyChosenTargetMustReceivePositiveDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Conflagrate()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryForX(
                player1, 0, 1, Map.of(bear.getId(), 0, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(Snapback.class)
    void losingOneTargetDoesNotRedistributeItsAssignedDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Conflagrate()));
        harness.setHand(player2, List.of(new Snapback()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.addMana(player2, ManaColor.BLUE, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorceryForX(player1, 0, 3, Map.of(bear.getId(), 2, player2.getId(), 1));
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ashcoat Bear");
        harness.assertLife(player2, lifeBefore - 1);
        harness.assertInGraveyard(player1, "Conflagrate");
    }

    @Test
    @CardUsed(IvoryMask.class)
    void normalCastCannotAssignDamageToAPlayerWithShroud() {
        harness.addToBattlefield(player2, new IvoryMask());
        harness.setHand(player1, List.of(new Conflagrate()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryForX(
                player1, 0, 1, Map.of(player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Conflagrate");
    }

    @Test
    @CardUsed(IvoryMask.class)
    void flashbackCannotAssignDamageToAPlayerWithShroud() {
        harness.addToBattlefield(player2, new IvoryMask());
        harness.setGraveyard(player1, List.of(new Conflagrate()));
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashbackForXWithDiscards(
                player1, 0, 1, Map.of(player2.getId(), 1), List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player1, "Conflagrate");
    }
}
