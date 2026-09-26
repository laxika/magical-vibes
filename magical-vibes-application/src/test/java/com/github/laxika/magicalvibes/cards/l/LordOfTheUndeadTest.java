package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DeepwoodGhoul;
import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordOfTheUndead.class, Gravedigger.class, DeepwoodGhoul.class, GrizzlyBears.class})
class LordOfTheUndeadTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new LordOfTheUndead(), "{1}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(LordOfTheUndead.class);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new LordOfTheUndead(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lord of the Undead");
    }

    @Test
    @DisplayName("Enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new LordOfTheUndead(), "{1}{B}{B}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Lord of the Undead");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Static effect: buffs other Zombies =====

    @Test
    @DisplayName("Other Zombie creatures get +1/+1")
    void buffsOtherZombies() {
        // Gravedigger is a Zombie (2/2)
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new LordOfTheUndead());

        Permanent gravedigger = findPermanent(player1, "Gravedigger");

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lord of the Undead does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new LordOfTheUndead());

        Permanent lord = findPermanent(player1, "Lord of the Undead");

        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Zombie creatures")
    void doesNotBuffNonZombies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LordOfTheUndead());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs opponent's Zombie creatures too")
    void buffsOpponentZombies() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player2, new Gravedigger());

        Permanent opponentZombie = findPermanent(player2, "Gravedigger");

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(3);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Lords of the Undead buff each other")
    void twoLordsBuffEachOther() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new LordOfTheUndead());

        List<Permanent> lords = findPermanents(player1, "Lord of the Undead");

        assertThat(lords).hasSize(2);
        for (Permanent lord : lords) {
            assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Two Lords give +2/+2 to other Zombies")
    void twoLordsStackBonuses() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new Gravedigger());

        Permanent gravedigger = findPermanent(player1, "Gravedigger");

        // 2/2 base + 2/2 from two lords = 4/4
        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(4);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Lord of the Undead leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new Gravedigger());

        Permanent gravedigger = findPermanent(player1, "Gravedigger");

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Lord of the Undead"));

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus applies when Lord resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.castFromHand(player1, new LordOfTheUndead(), "{1}{B}{B}");

        Permanent gravedigger = findPermanent(player1, "Gravedigger");

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new Gravedigger());

        Permanent gravedigger = findPermanent(player1, "Gravedigger");

        gravedigger.setPowerModifier(gravedigger.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(8); // 2 base + 5 spell + 1 static

        gravedigger.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(3);
    }

    // ===== Activated ability: activating =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Lord of the Undead");
    }

    @Test
    @DisplayName("Activating ability taps Lord of the Undead")
    void activatingTapsLord() {
        Permanent lord = addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));

        assertThat(lord.isTapped()).isFalse();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        assertThat(lord.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));

        // {1}{B} cost → 2 mana consumed, 2 remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Activated ability: resolution =====

    @Test
    @DisplayName("Returns Zombie card from graveyard to hand")
    void returnsZombieFromGraveyardToHand() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gravedigger");
        harness.assertNotInGraveyard(player1, "Gravedigger");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can choose specific Zombie when multiple are in graveyard")
    void choosesSpecificZombieFromGraveyard() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());
        Gravedigger gravedigger = new Gravedigger();
        DeepwoodGhoul target = new DeepwoodGhoul();
        harness.setGraveyard(player1, List.of(gravedigger, target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deepwood Ghoul");
        harness.assertInGraveyard(player1, "Gravedigger");
        harness.assertNotInGraveyard(player1, "Deepwood Ghoul");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Zombie card target is required when activating the ability")
    void requiresTargetWhenActivating() {
        Permanent lord = addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Gravedigger zombie = new Gravedigger();
        harness.setGraveyard(player1, List.of(zombie));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ability requires a target");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(lord.isTapped()).isFalse();
    }

    // ===== Activated ability: edge cases =====

    @Test
    @DisplayName("The ability cannot be activated without a graveyard card")
    void cannotActivateWithEmptyGraveyard() {
        Permanent lord = addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ability requires a target");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(lord.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A non-Zombie graveyard card cannot satisfy the target")
    void cannotActivateWithNoZombieInGraveyard() {
        Permanent lord = addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(lord.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only Zombie cards are selectable when graveyard has mixed cards")
    void cannotTargetNonZombieFromGraveyard() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        GrizzlyBears bears = new GrizzlyBears();
        Gravedigger zombie = new Gravedigger();
        harness.setGraveyard(player1, List.of(bears, zombie));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Zombie cards are not selectable as targets")
    void nonZombieCardsNotSelectable() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Activated ability: validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent lord = addReadyLord(player1);
        lord.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        // Add Lord with summoning sickness (creature with tap ability)
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new LordOfTheUndead());
        lord.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("The graveyard target is locked in when the ability is activated")
    void targetIsSelectedAtActivation() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Gravedigger target = new Gravedigger();
        DeepwoodGhoul otherZombie = new DeepwoodGhoul();
        harness.setGraveyard(player1, List.of(target, otherZombie));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherZombie, target));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gravedigger");
        harness.assertInGraveyard(player1, "Deepwood Ghoul");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability cannot target a Zombie in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Gravedigger");
    }

    @Test
    @DisplayName("The ability does not return a Zombie that left the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Gravedigger");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Lord remains on battlefield =====

    @Test
    @DisplayName("Lord of the Undead remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Gravedigger target = new Gravedigger();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lord of the Undead");
    }

    // ===== Helpers =====

    private Permanent addReadyLord(Player player) {
        return addCreatureReady(player, new LordOfTheUndead());
    }
}

