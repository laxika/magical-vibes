package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SecretDoor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrickstersTalisman.class, GrizzlyBears.class, SecretDoor.class})
class TrickstersTalismanTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTalisman(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger sacrifices the Talisman and creates a creature copy")
    void acceptingTriggerSacrificesTalismanAndCreatesCopy() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTalisman(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Trickster's Talisman");
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the combat-damage trigger leaves the Talisman attached")
    void decliningTriggerLeavesTalismanAttached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent talisman = attachTalisman(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(talisman);
        assertThat(talisman.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void equipCostsTwoAndMovesTheBonus() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent talisman = attachTalisman(player1, first);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(talisman.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void combatDamageToCreatureDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachTalisman(player1, attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SecretDoor());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void creatureControllerControlsGrantedTriggerAndCannotSacrificeOpponentsEquipment() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent talisman = attachTalisman(player1, creature);
        creature.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(talisman);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void movingEquipmentAfterDamageStillCopiesTheCreatureThatDealtDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new SecretDoor());
        Permanent talisman = attachTalisman(player1, creature);
        creature.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        talisman.setAttachedTo(other.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Trickster's Talisman");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList())
                .singleElement().satisfies(token ->
                        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    void creatureLeavingAfterDamageStillCreatesCopyUsingLastKnownInformation() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTalisman(player1, creature);
        creature.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, creature));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Trickster's Talisman");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
                });
    }

    @Test
    void equipmentLeavingAfterDamagePreventsCopyCreation() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent talisman = attachTalisman(player1, creature);
        creature.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, talisman));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Trickster's Talisman");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
    }

    private Permanent attachTalisman(Player player, Permanent creature) {
        Permanent talisman = harness.addToBattlefieldAndReturn(player, new TrickstersTalisman());
        talisman.setSummoningSick(false);
        talisman.setAttachedTo(creature.getId());
        return talisman;
    }
}
