package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({Demotion.class, BottleGnomes.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class})
class DemotionTest extends BaseCardTest {

    @Test
    void enchantedCreatureCannotBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        Permanent demotion = harness.addToBattlefieldAndReturn(player1, new Demotion());
        demotion.setAttachedTo(blocker.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        gnomes.setSummoningSick(false);

        Permanent demotion = harness.addToBattlefieldAndReturn(player2, new Demotion());
        demotion.setAttachedTo(gnomes.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Demotion()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, gd.playerBattlefields.get(player2.getId()).getFirst().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void resolvingAuraRestrictsOnlyEnchantedCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.setHand(player1, List.of(new Demotion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demotion");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enchanted);
    }

    @Test
    void enchantedCreatureCanStillAttack() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Demotion());
        aura.setAttachedTo(attacker.getId());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    void activatedAbilitiesBecomeAvailableAfterAuraLeaves() {
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Demotion());
        aura.setAttachedTo(gnomes.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player2.getId()).remove(aura);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Bottle Gnomes");
    }
    @Test
    void enchantedCreatureCannotActivateManaAbilities() {
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Demotion());
        aura.setAttachedTo(elves.getId());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(elves.isTapped()).isFalse();
    }
}
