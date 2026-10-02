package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.cards.s.SpringjackPasture;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecumbentBliss.class, NettleSentinel.class, SpringjackPasture.class})
class RecumbentBlissTest extends BaseCardTest {

    // ===== Combat lockdown =====

    @Test
    @DisplayName("Resolving Recumbent Bliss attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new NettleSentinel());

        harness.setHand(player1, List.of(new RecumbentBliss()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Recumbent Bliss")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new NettleSentinel());

        Permanent bliss = harness.addToBattlefieldAndReturn(player2, new RecumbentBliss());
        bliss.setAttachedTo(creature.getId());

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
        Permanent attacker = addCreatureReady(player1, new NettleSentinel());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new NettleSentinel());

        Permanent bliss = harness.addToBattlefieldAndReturn(player1, new RecumbentBliss());
        bliss.setAttachedTo(blocker.getId());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new NettleSentinel()); // a legal target exists, so the card is playable
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new SpringjackPasture());
        harness.setHand(player1, List.of(new RecumbentBliss()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Upkeep may gain life =====

    @Test
    @DisplayName("Controller may gain 1 life at their upkeep when accepting")
    void gainsLifeAtUpkeepWhenAccepting() {
        Permanent creature = addCreatureReady(player2, new NettleSentinel());
        Permanent bliss = harness.addToBattlefieldAndReturn(player1, new RecumbentBliss());
        bliss.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the upkeep may trigger → prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life gain when declining the upkeep may")
    void noLifeGainWhenDeclining() {
        Permanent creature = addCreatureReady(player1, new NettleSentinel());
        Permanent bliss = harness.addToBattlefieldAndReturn(player1, new RecumbentBliss());
        bliss.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }
}
