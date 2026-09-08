package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhastlyRemains.class, WalkingCorpse.class, GrizzlyBears.class})
class GhastlyRemainsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each Zombie card in your hand")
    void entersWithCountersForZombieCardsInHand() {
        GhastlyRemains card = new GhastlyRemains();
        harness.setHand(player1, List.of(card, new WalkingCorpse(), new WalkingCorpse(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent remains = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(remains.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("May pay {B}{B}{B} during your upkeep to return it from your graveyard to your hand")
    void paysToReturnFromGraveyardToHand() {
        GhastlyRemains card = new GhastlyRemains();
        harness.setGraveyard(player1, List.of(card));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(card.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(card.getId()));
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves it in the graveyard")
    void decliningPaymentLeavesItInGraveyard() {
        GhastlyRemains card = new GhastlyRemains();
        harness.setGraveyard(player1, List.of(card));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(card.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(card.getId()));
    }
}
