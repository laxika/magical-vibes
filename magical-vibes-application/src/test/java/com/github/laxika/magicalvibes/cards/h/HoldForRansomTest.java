package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({HoldForRansom.class, GrizzlyBears.class, FountainOfYouth.class, Mountain.class})
class HoldForRansomTest extends BaseCardTest {

    private Permanent attachTo(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HoldForRansom());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachTo(creature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void enchantedCreatureCannotBlock() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachTo(creature);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void creatureControllerCanPayToSacrificeAuraAndAuraControllerDraws() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachTo(creature);
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int auraControllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int creatureControllerHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hold for Ransom");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(creatureControllerHandBefore);
    }

    @Test
    void ransomAbilityCanOnlyBeActivatedAtSorcerySpeed() {
        addCreatureReady(player2, new GrizzlyBears());
        attachTo(gd.playerBattlefields.get(player2.getId()).getFirst());
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void cannotEnchantNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new HoldForRansom()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
