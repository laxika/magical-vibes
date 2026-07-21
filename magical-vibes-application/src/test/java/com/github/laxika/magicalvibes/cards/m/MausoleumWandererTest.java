package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MausoleumWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Another Spirit entering gives Mausoleum Wanderer +1/+1 until end of turn")
    void anotherSpiritEnteringBoosts() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        castChapelGeist(player1);
        harness.passBothPriorities(); // resolve Geist
        harness.passBothPriorities(); // resolve Wanderer trigger

        assertThat(wanderer.getPowerModifier()).isEqualTo(1);
        assertThat(wanderer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Spirit creature entering does not boost Mausoleum Wanderer")
    void nonSpiritDoesNotBoost() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(wanderer.getPowerModifier()).isEqualTo(0);
        assertThat(wanderer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The Spirit ETB boost wears off at end of turn")
    void spiritBoostWearsOffAtCleanup() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        castChapelGeist(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(wanderer.getPowerModifier()).isEqualTo(1);

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wanderer.getPowerModifier()).isEqualTo(0);
        assertThat(wanderer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrifice counters an instant when controller cannot pay power mana")
    void countersInstantWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new MausoleumWanderer());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1); // cast only

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mausoleum Wanderer"));
    }

    @Test
    @DisplayName("Boosted power raises the counter-unless-pays cost")
    void boostedPowerRaisesPayAmount() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new MausoleumWanderer());
        castChapelGeist(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(wanderer.getPowerModifier()).isEqualTo(1); // 2 power

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        // 1 to cast + 1 floating — not enough to pay {2}
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Can't afford {2} with 1 mana left → auto-countered
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    private void castChapelGeist(com.github.laxika.magicalvibes.model.Player controller) {
        harness.setHand(controller, List.of(new ChapelGeist()));
        harness.addMana(controller, ManaColor.WHITE, 2);
        harness.addMana(controller, ManaColor.COLORLESS, 1);
        harness.castCreature(controller, 0);
    }

    @Test
    @DisplayName("Spell survives when controller pays the snapshotted power amount")
    void notCounteredWhenControllerPaysPower() {
        harness.addToBattlefield(player1, new MausoleumWanderer());

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 2); // 1 cast + 1 pay

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        harness.addToBattlefield(player1, new MausoleumWanderer());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
