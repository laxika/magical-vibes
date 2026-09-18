package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MobilizerMech;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireNationSalvagers.class, GrizzlyBears.class, MobilizerMech.class})
class FireNationSalvagersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts a +1/+1 counter on target creature you control")
    void enteringPutsCounterOnTargetCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castSalvagers(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability cannot target an opponent's creature")
    void enteringCannotTargetOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireNationSalvagers()));
        addSalvagerMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle you control");
    }

    @Test
    @DisplayName("Combat damage from a creature with a counter returns a creature or Vehicle from the damaged player's graveyard")
    void counteredCreatureCombatDamageReturnsVehicle() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player2, List.of(vehicle));

        addCreatureReady(player1, new FireNationSalvagers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(vehicle.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(vehicle.getId()));
    }

    @Test
    @DisplayName("Combat damage from a creature without counters does not trigger")
    void creatureWithoutCountersDoesNotTrigger() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player2, List.of(vehicle));

        addCreatureReady(player1, new FireNationSalvagers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(vehicle);
    }

    private void castSalvagers(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FireNationSalvagers()));
        addSalvagerMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addSalvagerMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
