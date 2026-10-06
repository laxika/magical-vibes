package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.Afflict;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.m.MorbidHunger;
import com.github.laxika.magicalvibes.cards.s.ShiftingSky;
import com.github.laxika.magicalvibes.cards.z.ZombieCannibal;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PilgrimOfVirtue.class, DuskImp.class, DwarvenGrunt.class, Afflict.class, MorbidHunger.class, ShiftingSky.class, ZombieCannibal.class})
class PilgrimOfVirtueTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability sacrifices Pilgrim of Virtue")
    void activatingAbilitySacrificesPilgrim() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.assertNotOnBattlefield(player1, "Pilgrim of Virtue");
        harness.assertInGraveyard(player1, "Pilgrim of Virtue");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating the ability without white mana is not allowed")
    void requiresWhiteManaToActivate() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, pilgrim), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough mana to activate ability");
    }

    @Test
    @DisplayName("Resolving the ability only allows a black source to be chosen")
    void resolvingAbilityOnlyAllowsBlackSource() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        Permanent blackSource = addCreatureReady(player2, new DuskImp());
        addCreatureReady(player2, new DwarvenGrunt());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, blackSource.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .anyMatch(shield -> shield.sourceId().equals(blackSource.getId()));
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen black source")
    void preventsNextDamageFromChosenBlackSource() {
        harness.setLife(player1, 20);
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        Permanent blackSource = addCreatureReady(player2, new DuskImp());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackSource.getId());

        blackSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("A non-black source cannot be chosen")
    void nonBlackSourceCannotBeChosen() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        addCreatureReady(player2, new DwarvenGrunt());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Protection from black prevents a black spell from targeting Pilgrim of Virtue")
    void protectionFromBlackPreventsBlackSpellTargeting() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        harness.setHand(player2, List.of(new Afflict()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, pilgrim.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("A black spell on the stack can be chosen as the damage source")
    void preventsNextDamageFromChosenBlackSpell() {
        harness.setLife(player1, 20);
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        MorbidHunger morbidHunger = new MorbidHunger();
        harness.setHand(player2, List.of(morbidHunger));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(morbidHunger.getId());
        harness.handlePermanentChosen(player1, morbidHunger.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The chosen black source's damage to another player is prevented")
    void preventsDamageToAnotherPlayer() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        Permanent attacker = addCreatureReady(player1, new DuskImp());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player1);

        harness.assertLife(player2, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("A chosen source that stops being black deals damage without consuming the shield")
    void nonBlackDamageDoesNotConsumeShield() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        Permanent attacker = addCreatureReady(player2, new DuskImp());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        Permanent sky = harness.addToBattlefieldAndReturn(player1, new ShiftingSky());
        sky.setChosenColor(CardColor.RED);
        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);

        sky.setChosenColor(CardColor.BLACK);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Protection prevents combat damage from a black creature")
    void protectionPreventsBlackCombatDamage() {
        Permanent attacker = addCreatureReady(player2, new ZombieCannibal());
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        pilgrim.setBlocking(true);
        pilgrim.addBlockingTarget(0);

        harness.resolveCombatDamage();

        assertThat(pilgrim.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Pilgrim of Virtue");
        harness.assertInGraveyard(player2, "Zombie Cannibal");
    }

    @Test
    @DisplayName("A black creature cannot block Pilgrim of Virtue")
    void protectionPreventsBlackBlocking() {
        addCreatureReady(player1, new PilgrimOfVirtue());
        addCreatureReady(player2, new DuskImp());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("The shield prevents a black spell's damage to a creature")
    void preventsSpellDamageToCreature() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfVirtue());
        Permanent victim = addCreatureReady(player1, new DwarvenGrunt());
        MorbidHunger hunger = new MorbidHunger();
        harness.setHand(player2, List.of(hunger));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, victim.getId());
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, pilgrim), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, hunger.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dwarven Grunt");
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertLife(player2, 23);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
