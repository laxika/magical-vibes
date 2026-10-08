package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfDenial;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnscytheKillerOfKings.class, GrizzlyBears.class, WallOfDenial.class})
class UnscytheKillerOfKingsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger exiles the dying creature and creates a 2/2 black Zombie")
    void acceptExilesDyingCreatureAndCreatesZombie() {
        Permanent blocker = setUpEquippedKill(player1, player2);

        runCombatUntilMayPrompt();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // The dying creature's card is exiled (no longer in its owner's graveyard).
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(gd.exiledCards)
                .anyMatch(e -> e.card().getId().equals(blocker.getCard().getId()));
        // A 2/2 black Zombie token is created under Unscythe's controller.
        assertThat(zombieTokens(player1)).hasSize(1);
        assertThat(zombieTokens(player1).getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(zombieTokens(player1).getFirst().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the trigger leaves the creature in the graveyard and makes no token")
    void declineLeavesCreatureAndMakesNoZombie() {
        Permanent blocker = setUpEquippedKill(player1, player2);

        runCombatUntilMayPrompt();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(gd.exiledCards)
                .noneMatch(e -> e.card().getId().equals(blocker.getCard().getId()));
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("No trigger when the killing creature has no Unscythe attached")
    void noTriggerWhenEquipmentNotAttached() {
        // A creature that survives combat (toughness 5) but kills the 2/2 blocker, with Unscythe on
        // the battlefield unattached — the killer carries no such ability, so nothing triggers.
        GrizzlyBears killerCard = new GrizzlyBears();
        killerCard.setToughness(5);
        Permanent killer = harness.addToBattlefieldAndReturn(player1, killerCard);
        killer.setSummoningSick(false);
        killer.setAttacking(true);

        harness.addToBattlefield(player1, new UnscytheKillerOfKings());

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        // The killer had no such ability, so no "you may exile" trigger was ever offered.
        assertThat(gd.pendingMayAbilities)
                .noneMatch(a -> a.sourceCard().getName().equals("Unscythe, Killer of Kings"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(blocker.getCard().getId()));
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    void equipCostsTwoAndTransfersBonuses() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new UnscytheKillerOfKings());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        equipment.setAttachedTo(first.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FIRST_STRIKE)).isTrue();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void equipmentControllerGetsChoiceAndTokenInsteadOfCreatureController() {
        Permanent blocker = setUpEquippedKill(player1, player2);
        Permanent equipment = findPermanent(player1, "Unscythe, Killer of Kings");
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        runCombatUntilMayPrompt();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(blocker.getCard().getId()));
        assertThat(zombieTokens(player2)).hasSize(1);
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    void noTokenIfDyingCardHasLeftGraveyard() {
        Permanent blocker = setUpEquippedKill(player1, player2);
        runCombatUntilMayPrompt();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        gd.playerGraveyards.get(player2.getId()).removeIf(card -> card.getId().equals(blocker.getCard().getId()));
        harness.setExile(player2, java.util.List.of(blocker.getCard()));

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    void triggersForDamageDealtBeforeEquipmentWasAttached() {
        Permanent blocker = setUpEquippedKill(player1, player2, new WallOfDenial());
        Permanent equipment = findPermanent(player1, "Unscythe, Killer of Kings");
        var equippedId = equipment.getAttachedTo();
        equipment.setAttachedTo(null);
        harness.resolveCombatDamage();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        equipment.setAttachedTo(equippedId);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, blocker));
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(blocker.getCard().getId()));
        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    void doesNotTriggerForDamageByPreviouslyEquippedCreature() {
        Permanent blocker = setUpEquippedKill(player1, player2, new WallOfDenial());
        Permanent equipment = findPermanent(player1, "Unscythe, Killer of Kings");
        harness.resolveCombatDamage();
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        equipment.setAttachedTo(replacement.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, blocker));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    void triggersWhenEquipmentAndPreviouslyDamagedCreatureDieSimultaneously() {
        Permanent blocker = setUpEquippedKill(player1, player2, new WallOfDenial());
        Permanent equipment = findPermanent(player1, "Unscythe, Killer of Kings");
        harness.resolveCombatDamage();
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();

        var removal = harness.getPermanentRemovalService();
        harness.inMutationScope(() -> removal.performSimultaneousRemovals(
                gd, java.util.List.of(equipment, blocker), () -> {
                    removal.destroyPermanentToGraveyard(gd, equipment);
                    removal.destroyPermanentToGraveyard(gd, blocker);
                }));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(blocker.getCard().getId()));
        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    void doesNotTriggerWhileEquipmentHasLostItsAbilities() {
        Permanent blocker = setUpEquippedKill(player1, player2, new WallOfDenial());
        Permanent equipment = findPermanent(player1, "Unscythe, Killer of Kings");
        equipment.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.resolveCombatDamage();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, blocker));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    /**
     * Puts a Grizzly Bears equipped with Unscythe (a 5/5) on {@code attacker}'s battlefield attacking,
     * and a 2/2 Grizzly Bears on {@code defender}'s battlefield blocking it. Returns the blocker.
     */
    private Permanent setUpEquippedKill(Player attacker, Player defender) {
        return setUpEquippedKill(attacker, defender, new GrizzlyBears());
    }

    private Permanent setUpEquippedKill(Player attacker, Player defender, Card blockerCard) {
        Permanent equipped = harness.addToBattlefieldAndReturn(attacker, new GrizzlyBears());
        equipped.setSummoningSick(false);
        equipped.setAttacking(true);

        Permanent unscythe = harness.addToBattlefieldAndReturn(attacker, new UnscytheKillerOfKings());
        unscythe.setAttachedTo(equipped.getId());

        Permanent blocker = harness.addToBattlefieldAndReturn(defender, blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        return blocker;
    }

    private void runCombatUntilMayPrompt() {
        for (int i = 0; i < 10 && !gd.interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }
    }

    private java.util.List<Permanent> zombieTokens(Player player) {
        return findPermanents(player, "Zombie");
    }
}
