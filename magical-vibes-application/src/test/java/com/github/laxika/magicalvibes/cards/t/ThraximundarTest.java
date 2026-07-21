package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThraximundarTest extends BaseCardTest {

    private Permanent thraximundar() {
        return findPermanent(player1, "Thraximundar");
    }

    // ===== "Whenever a player sacrifices a creature, you may put a +1/+1 counter" =====

    @Test
    @DisplayName("When another player sacrifices a creature, may put a +1/+1 counter on Thraximundar (accept)")
    void anyPlayerSacrificesAccept() {
        addCreatureReady(player1, new Thraximundar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        // Resolve Cruel Edict → player2 sacrifices Grizzly Bears → Thraximundar's may prompt.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(thraximundar().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the sacrifice trigger leaves Thraximundar without a counter")
    void anyPlayerSacrificesDecline() {
        addCreatureReady(player1, new Thraximundar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(thraximundar().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller's own sacrifice also triggers the counter (any player)")
    void controllerOwnSacrificeTriggers() {
        addCreatureReady(player1, new Thraximundar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castEdictAt(player1);
        harness.passBothPriorities();

        // player1 controls both Thraximundar and Grizzly Bears, so Cruel Edict prompts a choice.
        harness.handlePermanentChosen(player1, bears.getId());
        // Resolve the queued sacrifice trigger so its "you may" surfaces.
        harness.passBothPriorities();

        // Sacrificing the controller's own creature still triggers Thraximundar's counter ability.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(thraximundar().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    // ===== "Whenever Thraximundar attacks, defending player sacrifices a creature" =====

    @Test
    @DisplayName("Attacking makes the defending player sacrifice their lone creature, then Thraximundar may grow")
    void attackForcesDefenderSacrificeThenCounter() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        // Resolve the attack edict → player2 auto-sacrifices its only creature.
        harness.passBothPriorities();
        harness.passBothPriorities();

        // The defending player's creature is gone; the attacker's own creature is untouched.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);

        // The sacrifice also triggers the +1/+1 counter ability for the attacker.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The defending player chooses which creature to sacrifice when they control several")
    void attackDefenderChoosesWhichToSacrifice() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // The defending player (player2) is prompted to choose the creature to sacrifice.
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, hawk.getId());

        // The chosen creature is sacrificed; the other survives.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Resolve the queued sacrifice trigger so its "you may" surfaces.
        harness.passBothPriorities();

        // The interactive sacrifice choice also triggers Thraximundar's +1/+1 counter ability.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking a defender with no creatures sacrifices nothing and grants no counter")
    void attackDefenderWithNoCreatures() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    // ===== Helpers =====

    private void castEdictAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, target.getId());
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
