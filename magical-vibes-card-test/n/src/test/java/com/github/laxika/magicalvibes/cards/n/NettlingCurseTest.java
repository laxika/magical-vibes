package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.c.CytoplastManipulator;
import com.github.laxika.magicalvibes.cards.s.SimicGuildmage;
import com.github.laxika.magicalvibes.cards.v.ValorMadeReal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NettlingCurse.class, AssaultZeppelid.class, AzoriusSignet.class,
        CytoplastManipulator.class, SimicGuildmage.class, ValorMadeReal.class})
class NettlingCurseTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature attacking makes its controller lose 3 life")
    void attackingLosesThreeLife() {
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        attachCurse(player1, creature);

        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Enchanted creature blocking makes its controller lose 3 life")
    void blockingLosesThreeLife() {
        addCreatureReady(player1, new AssaultZeppelid());
        Permanent blocker = addCreatureReady(player2, new AssaultZeppelid());
        attachCurse(player2, blocker);

        int lifeBefore = gd.getLife(player2.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("The activated ability makes the enchanted creature attack this turn if able")
    void activatedAbilityMakesEnchantedCreatureAttack() {
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        Permanent aura = attachCurse(player1, creature);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Nettling Curse can enchant only a creature")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AzoriusSignet());
        harness.setHand(player1, List.of(new NettlingCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The enchanted creature's controller loses life even when an opponent controls the Aura")
    void attackingCreatureControllerLosesLifeWhenAuraIsOpponentControlled() {
        Permanent creature = addCreatureReady(player2, new AssaultZeppelid());
        attachCurse(player1, creature);

        int lifeBefore = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("The attack trigger makes the creature's current controller lose life")
    void attackTriggerUsesControllerAtResolution() {
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attachCurse(player1, creature);
        Permanent manipulator = addCreatureReady(player2, new CytoplastManipulator());
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int originalControllerLife = gd.getLife(player1.getId());
        int newControllerLife = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, creature.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getLife(player1.getId())).isEqualTo(originalControllerLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(newControllerLife - 3);
    }

    @Test
    @DisplayName("Blocking multiple attackers causes only one loss of 3 life")
    void blockingMultipleAttackersTriggersOnce() {
        addCreatureReady(player1, new AssaultZeppelid());
        addCreatureReady(player1, new AssaultZeppelid());
        Permanent blocker = addCreatureReady(player2, new AssaultZeppelid());
        attachCurse(player1, blocker);
        harness.setHand(player2, List.of(new ValorMadeReal()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, blocker.getId());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
            resolveAllTriggers();
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Moving the Aura in response makes its activated ability affect the new host")
    void activatedAbilityUsesEnchantedCreatureAtResolution() {
        Permanent originalHost = addCreatureReady(player1, new AssaultZeppelid());
        Permanent newHost = addCreatureReady(player1, new AssaultZeppelid());
        Permanent aura = attachCurse(player1, originalHost);
        Permanent guildmage = addCreatureReady(player1, new SimicGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 2, null, null);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guildmage),
                1, null, aura.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, newHost.getId());
        resolveAllTriggers();

        assertThat(aura.getAttachedTo()).isEqualTo(newHost.getId());
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A summoning-sick enchanted creature is not forced to attack")
    void activatedAbilityDoesNotOverrideSummoningSickness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        creature.setSummoningSick(true);
        Permanent aura = attachCurse(player1, creature);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();
        int lifeBefore = gd.getLife(player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting Nettling Curse attaches it to the targeted opposing creature")
    void castingAttachesCurseAndTriggersOnAttack() {
        Permanent creature = addCreatureReady(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new NettlingCurse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Nettling Curse").getAttachedTo()).isEqualTo(creature.getId());
        int lifeBefore = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    private Permanent attachCurse(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new NettlingCurse());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
