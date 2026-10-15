package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.s.ShivanFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HealingGrace.class, GrizzlyBears.class, MoggFanatic.class, ShivanFire.class})
class HealingGraceTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Healing Grace targeting a player puts it on the stack")
    void castTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Healing Grace targeting a creature puts it on the stack")
    void castTargetingCreaturePutsOnStack() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bear.getId());
    }

    // ===== Resolution — source choice and life gain =====

    @Test
    @DisplayName("Resolving Healing Grace prompts for source choice")
    void resolvingPromptsForSourceChoice() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
    }

    @Test
    @DisplayName("Choosing a source creates a target-source prevention shield")
    void choosingSourceCreatesShield() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.targetSourceDamagePreventionShields).hasSize(1);
        assertThat(gd.targetSourceDamagePreventionShields.getFirst().targetId()).isEqualTo(player1.getId());
        assertThat(gd.targetSourceDamagePreventionShields.getFirst().sourceId()).isEqualTo(opponentCreature.getId());
        assertThat(gd.targetSourceDamagePreventionShields.getFirst().remainingAmount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving Healing Grace gains 3 life for the caster")
    void resolvingGains3Life() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 17);
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Prevention — combat damage to player =====

    @Test
    @DisplayName("Shield prevents combat damage from chosen source to target player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        // Cast Healing Grace targeting player1, choose opponent's creature as source
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Combat: opponent's Grizzly Bears (2/2) attacks player1
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        resolveCombat(player2);

        // All 2 damage prevented (shield has 3 capacity)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Shield partially consumed when source deals more than 3 damage")
    void partialPreventionWhenSourceDealsMoreThan3() {
        harness.setLife(player1, 20);
        Permanent bigCreature = addReadyCreatureWithStats(player2, 5, 5);

        // Cast Healing Grace targeting player1, choose big creature as source
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, bigCreature.getId());

        // Combat: 5/5 creature attacks player1
        harness.forceActivePlayer(player2);
        bigCreature.setAttacking(true);
        resolveCombat(player2);

        // 5 damage - 3 prevented = 2 effective damage, but also +3 life from Healing Grace
        // Start: 20, +3 life = 23, -2 damage = 21
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    // ===== Prevention — combat damage to creature =====

    @Test
    @DisplayName("Shield prevents combat damage from chosen source to target creature")
    void preventsCombatDamageToCreature() {
        Permanent myCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreatureWithStats(player2, 3, 3);

        // Cast Healing Grace targeting my creature, choose opponent's 3/3 as source
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, myCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Combat: my 2/2 blocks opponent's 3/3
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        myCreature.setBlocking(true);
        myCreature.addBlockingTarget(0);
        resolveCombat(player2);

        // My 2/2 takes 3 damage from the 3/3, but 3 is prevented → creature survives
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    // ===== Non-matching source =====

    @Test
    @DisplayName("Shield does not prevent damage from non-matching source")
    void doesNotPreventFromNonMatchingSource() {
        harness.setLife(player1, 20);
        Permanent creature1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature2 = addReadyCreatureWithStats(player2, 3, 3);

        // Cast Healing Grace targeting player1, choose creature1 as source
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, creature1.getId());

        // Combat: creature2 (not the chosen source) attacks player1
        harness.forceActivePlayer(player2);
        creature2.setAttacking(true);
        resolveCombat(player2);

        // Player1 takes full 3 damage from creature2 (not the chosen source)
        // Life: 20 +3 (Healing Grace) -3 (combat) = 20
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Shield should still be active (not consumed)
        assertThat(gd.targetSourceDamagePreventionShields).hasSize(1);
    }

    // ===== Shield cleanup =====

    @Test
    @DisplayName("Target-source prevention shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.targetSourceDamagePreventionShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.targetSourceDamagePreventionShields).isEmpty();
    }

    // ===== Graveyard =====

    @Test
    @DisplayName("Healing Grace goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        harness.assertInGraveyard(player1, "Healing Grace");
    }

    @Test
    @DisplayName("A sacrificed creature whose ability is on the stack can be chosen as the source")
    void canChooseSacrificedAbilitySource() {
        Permanent source = addCreatureReady(player2, new MoggFanatic());
        addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(source.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("A spell on the stack can be chosen and its damage to a creature prevented")
    void preventsDamageFromChosenSpell() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        ShivanFire source = new ShivanFire();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(source));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("The caster gains life even when protecting the opponent")
    void targetingOpponentStillGainsLifeForCaster() {
        Permanent source = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An illegal sole target prevents both prevention and life gain")
    void illegalTargetDoesNotGainLife() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.targetSourceDamagePreventionShields).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Healing Grace");
    }

    private Permanent addReadyCreatureWithStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
