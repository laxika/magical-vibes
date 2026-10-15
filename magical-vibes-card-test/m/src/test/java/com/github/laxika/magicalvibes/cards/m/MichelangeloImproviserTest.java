package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarReborn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MichelangeloImproviser.class, GrizzlyBears.class, Forest.class, MouserMarkIII.class,
        LlanowarReborn.class})
class MichelangeloImproviserTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may put one creature and one land from hand onto the battlefield")
    void combatDamagePutsOneCreatureAndOneLandFromHand() {
        Permanent michelangelo = addReadyMichelangelo();
        michelangelo.setAttacking(true);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card extraCreature = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(creature, land, extraCreature)));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(extraCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature)
                .anyMatch(permanent -> permanent.getCard() == land);
    }

    @Test
    @DisplayName("The combat trigger puts no cards onto the battlefield when declined")
    void decliningCombatTriggerDoesNothing() {
        Permanent michelangelo = addReadyMichelangelo();
        michelangelo.setAttacking(true);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(creature, land)));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature || permanent.getCard() == land);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and enters tapped and attacking")
    void sneakSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new MichelangeloImproviser()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent michelangelo = findPermanent(player1, "Michelangelo, Improviser");
        assertThat(michelangelo.isTapped()).isTrue();
        assertThat(michelangelo.isAttacking()).isTrue();
        assertThat(michelangelo.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The creature choice may be declined while still putting a land onto the battlefield")
    void canPutOnlyLand() {
        Permanent michelangelo = addReadyMichelangelo();
        michelangelo.setAttacking(true);
        Card creature = new MouserMarkIII();
        Card land = new Forest();
        harness.setHand(player1, List.of(creature, land));

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Mouser Mark III");
    }

    @Test
    @DisplayName("The land choice may be declined after putting a creature onto the battlefield")
    void canPutOnlyCreature() {
        Permanent michelangelo = addReadyMichelangelo();
        michelangelo.setAttacking(true);
        Card creature = new MouserMarkIII();
        Card land = new Forest();
        harness.setHand(player1, List.of(creature, land));

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        Permanent entered = findPermanent(player1, "Mouser Mark III");
        assertThat(entered.isTapped()).isFalse();
        assertThat(entered.isAttacking()).isFalse();
        assertThat(entered.isSummoningSick()).isTrue();
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Accepting both choices with an empty hand completes the ability")
    void emptyHandDoesNotPreventCompletion() {
        Permanent michelangelo = addReadyMichelangelo();
        michelangelo.setAttacking(true);
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(michelangelo);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A graft land entering with the chosen creature sees that creature enter")
    void creatureAndLandEnterSimultaneously() {
        Permanent michelangelo = addReadyMichelangelo();
        michelangelo.setAttacking(true);
        Card creature = new MouserMarkIII();
        Card land = new LlanowarReborn();
        harness.setHand(player1, List.of(creature, land));

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Llanowar Reborn")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Mouser Mark III")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyMichelangelo() {
        return addCreatureReady(player1, new MichelangeloImproviser());
    }
}
