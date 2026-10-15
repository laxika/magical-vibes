package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FootChopper.class, GrizzlyBears.class, Forest.class})
class FootChopperTest extends BaseCardTest {

    @Test
    @DisplayName("Living weapon creates a 1/1 Ninja and gives it flying")
    void livingWeaponCreatesNinjaWithFlying() {
        harness.setHand(player1, List.of(new FootChopper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent equipment = findPermanent(player1, "Foot Chopper");
        Permanent ninja = findPermanent(player1, "Ninja");
        assertThat(equipment.getAttachedTo()).isEqualTo(ninja.getId());
        assertThat(ninja.getCard().getPower()).isEqualTo(1);
        assertThat(ninja.getCard().getToughness()).isEqualTo(1);
        assertThat(ninja.getCard().getSubtypes()).contains(CardSubtype.NINJA);
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger sacrifices the equipped creature and draws its power")
    void acceptingTriggerSacrificesCreatureAndDrawsPower() {
        Permanent creature = attachFootChopperToBears(player1);
        creature.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the combat-damage trigger leaves the equipped creature attached")
    void decliningTriggerLeavesCreatureAttached() {
        Permanent creature = attachFootChopperToBears(player1);
        Permanent equipment = findPermanent(player1, "Foot Chopper");
        creature.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent attachFootChopperToBears(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new FootChopper());
        equipment.setSummoningSick(false);
        equipment.setAttachedTo(creature.getId());
        return creature;
    }

    @Test
    void equipMovesEquipmentAndFlyingToNewCreature() {
        Permanent original = attachFootChopperToBears(player1);
        Permanent equipment = findPermanent(player1, "Foot Chopper");
        Permanent next = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, next.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(next.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, next, Keyword.FLYING)).isTrue();
    }

    @Test
    void combatDamageToCreatureDoesNotTriggerSacrifice() {
        Permanent blocker = attachFootChopperToBears(player2);
        blocker.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(attacker.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    void drawsUsingPowerAtResolutionRatherThanDamageDealt() {
        Permanent creature = attachFootChopperToBears(player1);
        creature.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        resolveCombatAndTrigger();
        creature.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(findPermanent(player1, "Foot Chopper").getAttachedTo()).isNull();
    }

    @Test
    void triggerStillSacrificesOriginalCreatureAfterEquipmentMoves() {
        Permanent original = attachFootChopperToBears(player1);
        Permanent next = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = findPermanent(player1, "Foot Chopper");
        original.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        resolveCombatAndTrigger();
        equipment.setAttachedTo(next.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(original).contains(next, equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(next.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotSacrificeEquippedCreatureControlledByOpponent() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new FootChopper());
        equipment.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void noDrawWhenDamageDealingCreatureHasLeftBattlefield() {
        Permanent creature = attachFootChopperToBears(player1);
        creature.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        resolveCombatAndTrigger();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        findPermanent(player1, "Foot Chopper").setAttachedTo(null);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
