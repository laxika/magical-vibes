package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscipleOfTheRingTest extends BaseCardTest {

    private static final int COUNTER_ABILITY = 0;
    private static final int PUMP_ABILITY = 1;
    private static final int TAP_ABILITY = 2;
    private static final int UNTAP_ABILITY = 3;

    @Test
    @DisplayName("Counter mode counters a noncreature spell whose controller cannot pay {2}")
    void counterModeCountersNoncreatureSpell() {
        harness.addToBattlefield(player1, new DiscipleOfTheRing());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, COUNTER_ABILITY, null, shock.getId(), Zone.STACK);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        // player2 spent their only mana casting Shock, so the ransom is unpayable
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter mode counters the spell when its controller declines to pay {2}")
    void counterModeCountersWhenControllerDeclines() {
        harness.addToBattlefield(player1, new DiscipleOfTheRing());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, COUNTER_ABILITY, null, shock.getId(), Zone.STACK);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter mode leaves the spell on the stack when its controller pays {2}")
    void counterModeSpellSurvivesWhenPaid() {
        harness.addToBattlefield(player1, new DiscipleOfTheRing());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 3); // 1 to cast, 2 to pay the ransom

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, COUNTER_ABILITY, null, shock.getId(), Zone.STACK);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("Counter mode cannot target a creature spell")
    void counterModeRejectsCreatureSpell() {
        harness.addToBattlefield(player1, new DiscipleOfTheRing());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, COUNTER_ABILITY, null, bears.getId(), Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pump mode gives +1/+1 until end of turn")
    void pumpModeBoostsSelfUntilEndOfTurn() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfTheRing());
        harness.setGraveyard(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, PUMP_ABILITY, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(4);
    }

    @Test
    @DisplayName("Tap mode taps the target creature and exiles the cost card")
    void tapModeTapsTargetCreature() {
        harness.addToBattlefield(player1, new DiscipleOfTheRing());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, TAP_ABILITY, null, bears.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Untap mode untaps the target creature")
    void untapModeUntapsTargetCreature() {
        harness.addToBattlefield(player1, new DiscipleOfTheRing());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, UNTAP_ABILITY, null, bears.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A sorcery card in the graveyard also pays the exile cost")
    void sorceryPaysTheExileCost() {
        harness.addToBattlefield(player1, new DiscipleOfTheRing());
        harness.setGraveyard(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, TAP_ABILITY, null, bears.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Divination"));
    }

    @Test
    @DisplayName("Cannot activate without an instant or sorcery card in the graveyard")
    void cannotActivateWithoutInstantOrSorceryInGraveyard() {
        harness.addToBattlefield(player1, new DiscipleOfTheRing());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, TAP_ABILITY, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
