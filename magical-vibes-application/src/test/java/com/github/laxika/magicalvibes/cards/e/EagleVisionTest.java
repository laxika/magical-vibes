package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EagleVision.class, GrizzlyBears.class})
class EagleVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards")
    void drawsThreeCards() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setHand(player1, List.of(new EagleVision()));
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Freerunning draws three cards after Assassin combat damage")
    void freerunningDrawsThreeCardsAfterAssassinDamage() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
        harness.setHand(player1, List.of(new EagleVision()));
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Freerunning is unavailable without qualifying combat damage")
    void freerunningRequiresQualifyingCombatDamage() {
        harness.setHand(player1, List.of(new EagleVision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    void freerunningDrawsAfterCommanderCombatDamageEvenAfterSourceLeaves() {
        UUID sourceId = UUID.randomUUID();
        gd.damageSourcesControlledByPlayerThisTurn.put(player1.getId(), Set.of(sourceId));
        gd.combatDamageToPlayersThisTurn.put(sourceId, Set.of(player2.getId()));
        gd.combatDamageSourcesThatWereCommandersThisTurn.add(sourceId);
        EagleVision first = new EagleVision();
        EagleVision second = new EagleVision();
        EagleVision third = new EagleVision();
        harness.setHand(player1, List.of(new EagleVision()));
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        harness.assertInGraveyard(player1, "Eagle Vision");
    }

    @Test
    void opponentsAssassinCombatDamageDoesNotEnableFreerunning() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player2.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
        harness.setHand(player1, List.of(new EagleVision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
        harness.assertInHand(player1, "Eagle Vision");
    }

    @Test
    void freerunningStillRequiresBlueMana() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
        harness.setHand(player1, List.of(new EagleVision()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Eagle Vision");
    }
}
