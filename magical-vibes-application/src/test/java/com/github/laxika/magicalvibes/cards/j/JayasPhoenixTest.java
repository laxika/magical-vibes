package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JayasPhoenix.class, ChandraNovicePyromancer.class, JaceBeleren.class, AirElemental.class, GrizzlyBears.class})
class JayasPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the next loyalty ability resolve twice")
    void copiesNextLoyaltyAbility() {
        Permanent phoenix = addCreatureReady(player1, new JayasPhoenix());
        Permanent chandra = addReadyChandra(player1, 5);
        Permanent elemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 0, null, null);
        resolveAllTriggers();

        assertThat(elemental.getPowerModifier()).isEqualTo(4);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("Casting a planeswalker lets the Phoenix return from the graveyard")
    void returnsFromGraveyardOnPlaneswalkerCast() {
        JayasPhoenix phoenix = new JayasPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.setHand(player1, List.of(new JaceBeleren()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Casting a non-planeswalker spell does not trigger the graveyard ability")
    void nonPlaneswalkerCastDoesNotTrigger() {
        JayasPhoenix phoenix = new JayasPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent chandra = harness.addToBattlefieldAndReturn(player, new ChandraNovicePyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, loyalty);
        chandra.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return chandra;
    }
}
