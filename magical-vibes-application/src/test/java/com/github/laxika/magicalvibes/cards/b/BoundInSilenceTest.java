package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoundInSilence.class, GrizzlyBears.class, FountainOfYouth.class})
class BoundInSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Bound in Silence attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player2);

        castAuraOn(bears);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BoundInSilence
                        && permanent.isAttached()
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature cannot be declared as an attacker")
    void enchantedCreatureCannotAttack() {
        Permanent bears = addCreatureReady(player1);
        attachAura(player2, bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot be declared as a blocker")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2);
        attachAura(player1, blocker);
        Permanent attacker = addCreatureReady(player1);
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
    @DisplayName("Removing Bound in Silence restores the creature's combat abilities")
    void removingAuraRestoresCombatAbilities() {
        Permanent bears = addCreatureReady(player1);
        Permanent aura = attachAura(player2, bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Bound in Silence fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = addCreatureReady(player2);

        harness.setHand(player1, List.of(new BoundInSilence()));
        addMana();
        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Bound in Silence cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new BoundInSilence()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castAuraOn(Permanent target) {
        harness.setHand(player1, List.of(new BoundInSilence()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent attachAura(com.github.laxika.magicalvibes.model.Player controller, Permanent target) {
        Permanent aura = new Permanent(new BoundInSilence());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
