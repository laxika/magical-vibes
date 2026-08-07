package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OneThousandLashesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches the Aura to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new OneThousandLashes()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("One Thousand Lashes")
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachLashes(player2, bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachLashes(player1, blocker);

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
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        attachLashes(player2, gnomes);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted creature's controller loses 1 life at their upkeep")
    void controllerLosesOneLifeAtUpkeep() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        attachLashes(player1, bears);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("No life loss during the Aura controller's upkeep")
    void noLifeLossDuringAuraControllerUpkeep() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        attachLashes(player1, bears);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        harness.setHand(player1, List.of(new OneThousandLashes()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachLashes(com.github.laxika.magicalvibes.model.Player controller, Permanent enchanted) {
        Permanent aura = new Permanent(new OneThousandLashes());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
