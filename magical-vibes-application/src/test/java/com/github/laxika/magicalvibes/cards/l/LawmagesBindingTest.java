package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LawmagesBinding.class, GrizzlyBears.class, BottleGnomes.class, FountainOfYouth.class,
        LlanowarElves.class, ProdigalPyromancer.class})
class LawmagesBindingTest extends BaseCardTest {

    @Test
    void enchantedCreatureCannotAttack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        addAttachedBinding(creature, player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void enchantedCreatureCannotBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        addAttachedBinding(blocker, player1);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void enchantedCreatureCannotActivateAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        creature.setSummoningSick(false);
        addAttachedBinding(creature, player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new LawmagesBinding()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canCastDuringOpponentsUpkeepAndAttachToTheirCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player2, List.of(new LawmagesBinding()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anySatisfy(binding -> assertThat(binding.getAttachedTo()).isEqualTo(creature.getId()));
    }

    @Test
    void enchantedCreatureCannotActivateManaAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        creature.setSummoningSick(false);
        addAttachedBinding(creature, player2);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void abilityAlreadyOnStackStillResolvesAfterBindingEnters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        creature.setSummoningSick(false);
        harness.setHand(player2, List.of(new LawmagesBinding()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Lawmage's Binding");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void activatedAbilitiesBecomeAvailableWhenBindingLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        Permanent binding = addAttachedBinding(creature, player2);
        gd.playerBattlefields.get(player2.getId()).remove(binding);
        gd.playerGraveyards.get(player2.getId()).add(binding.getCard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Bottle Gnomes");
    }

    private Permanent addAttachedBinding(Permanent creature, com.github.laxika.magicalvibes.model.Player controller) {
        Permanent binding = harness.addToBattlefieldAndReturn(controller, new LawmagesBinding());
        binding.setAttachedTo(creature.getId());
        return binding;
    }
}
