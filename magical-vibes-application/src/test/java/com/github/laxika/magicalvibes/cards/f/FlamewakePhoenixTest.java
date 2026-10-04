package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.b.BallistaCharger;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamewakePhoenix.class, BearCub.class, BallistaCharger.class})
class FlamewakePhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers from the graveyard at the beginning of the controller's combat with ferocious")
    void triggersWithFerocious() {
        addFerociousCreature();
        harness.setGraveyard(player1, List.of(new FlamewakePhoenix()));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Does not trigger without a creature with power 4 or greater")
    void doesNotTriggerWithoutFerocious() {
        harness.setGraveyard(player1, List.of(new FlamewakePhoenix()));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() != null
                && entry.getCard().getClass() == FlamewakePhoenix.class);
    }

    @Test
    @DisplayName("Paying {R} returns Flamewake Phoenix from the graveyard to the battlefield")
    void payingRedReturnsPhoenix() {
        FlamewakePhoenix phoenix = new FlamewakePhoenix();
        addFerociousCreature();
        harness.setGraveyard(player1, List.of(phoenix));

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Declining the payment leaves Flamewake Phoenix in the graveyard")
    void decliningKeepsPhoenixInGraveyard() {
        FlamewakePhoenix phoenix = new FlamewakePhoenix();
        addFerociousCreature();
        harness.setGraveyard(player1, List.of(phoenix));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Rechecks ferocious before paying and returning the Phoenix")
    void rechecksFerociousAtResolution() {
        FlamewakePhoenix phoenix = new FlamewakePhoenix();
        Permanent ferociousCreature = addFerociousCreature();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.addMana(player1, ManaColor.RED, 1);

        advanceToCombat(player1);
        ferociousCreature.setPowerModifier(-2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
    }

    @Test
    void mustAttackWhenAble() {
        addCreatureReady(player1, new FlamewakePhoenix());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void tappedPhoenixDoesNotHaveToAttack() {
        Permanent phoenix = addCreatureReady(player1, new FlamewakePhoenix());
        phoenix.tap();

        declareAttackers(List.of());

        assertThat(phoenix.isAttacking()).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentCombat() {
        addFerociousCreature();
        harness.setGraveyard(player1, List.of(new FlamewakePhoenix()));

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Flamewake Phoenix");
    }

    @Test
    void opponentsCreatureDoesNotEnableFerocious() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BearCub());
        creature.setPowerModifier(2);
        harness.setGraveyard(player1, List.of(new FlamewakePhoenix()));

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void uncrewedVehicleDoesNotEnableFerocious() {
        harness.addToBattlefield(player1, new BallistaCharger());
        harness.setGraveyard(player1, List.of(new FlamewakePhoenix()));

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void returnedPhoenixCanAttackImmediately() {
        addFerociousCreature();
        harness.setGraveyard(player1, List.of(new FlamewakePhoenix()));

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isAttacking()).isTrue();
    }
    private Permanent addFerociousCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearCub());
        creature.setPowerModifier(2);
        return creature;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
