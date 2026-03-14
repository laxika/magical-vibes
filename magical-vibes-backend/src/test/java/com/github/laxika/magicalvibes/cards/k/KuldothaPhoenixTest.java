package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KuldothaPhoenixTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Has GRAVEYARD_UPKEEP_TRIGGERED effect with MetalcraftConditionalEffect wrapping MayPayManaEffect")
    void hasCorrectEffects() {
        KuldothaPhoenix card = new KuldothaPhoenix();

        assertThat(card.getEffects(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);
        MetalcraftConditionalEffect metalcraft = (MetalcraftConditionalEffect) card.getEffects(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(MayPayManaEffect.class);
        MayPayManaEffect mayPay = (MayPayManaEffect) metalcraft.wrapped();
        assertThat(mayPay.manaCost()).isEqualTo("{4}");
        assertThat(mayPay.wrapped()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    @Test
    @DisplayName("Triggers during upkeep when in graveyard with metalcraft met")
    void triggersDuringUpkeepWithMetalcraft() {
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));

        advanceToUpkeep(player1);
        // Resolve MayPayManaEffect from stack
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getName()).isEqualTo("Kuldotha Phoenix");
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{4}");
    }

    @Test
    @DisplayName("Does not trigger when metalcraft is not met (fewer than 3 artifacts)")
    void doesNotTriggerWithoutMetalcraft() {
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));

        advanceToUpkeep(player2);

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Accepting and paying {4} returns Kuldotha Phoenix from graveyard to battlefield")
    void acceptingReturnsPhoenixToBattlefield() {
        KuldothaPhoenix phoenix = new KuldothaPhoenix();
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setGraveyard(player1, List.of(phoenix));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        // Resolve MayPayManaEffect from stack
        harness.passBothPriorities();
        // Accept — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Declining keeps Kuldotha Phoenix in graveyard")
    void decliningKeepsPhoenixInGraveyard() {
        KuldothaPhoenix phoenix = new KuldothaPhoenix();
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setGraveyard(player1, List.of(phoenix));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        // Resolve MayPayManaEffect from stack
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phoenix.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Kuldotha Phoenix"));
    }

    @Test
    @DisplayName("Cannot return if player cannot pay {4}")
    void cannotReturnWithoutMana() {
        KuldothaPhoenix phoenix = new KuldothaPhoenix();
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.setGraveyard(player1, List.of(phoenix));
        // No mana added

        advanceToUpkeep(player1);
        // Resolve MayPayManaEffect from stack
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Phoenix stays in graveyard because mana cannot be paid
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phoenix.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Kuldotha Phoenix"));
    }
}
