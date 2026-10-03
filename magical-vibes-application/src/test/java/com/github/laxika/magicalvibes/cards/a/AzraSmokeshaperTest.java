package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzraSmokeshaper.class, UniversalAutomaton.class})
class AzraSmokeshaperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants indestructible to a target creature you control")
    void etbGrantsIndestructible() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton());
        harness.setHand(player1, List.of(new AzraSmokeshaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, automaton.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, automaton, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton());
        harness.setHand(player1, List.of(new AzraSmokeshaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, automaton.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, automaton, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Azra tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent automaton = addCreatureReady(player1, new UniversalAutomaton());
        addCreatureReady(player2, new UniversalAutomaton());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.setHand(player1, List.of(new AzraSmokeshaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, automaton.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Universal Automaton");
        Permanent azra = findPermanent(player1, "Azra Smokeshaper");
        assertThat(azra.isTapped()).isTrue();
        assertThat(azra.isAttacking()).isTrue();
        assertThat(azra.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The ETB cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentAutomaton = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());
        harness.setHand(player1, List.of(new AzraSmokeshaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentAutomaton.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A normally cast Azra can target itself with its enter trigger")
    void castAzraCanProtectItself() {
        harness.setHand(player1, List.of(new AzraSmokeshaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent azra = findPermanent(player1, "Azra Smokeshaper");
        harness.handlePermanentChosen(player1, azra.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, azra, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Ninjutsu triggers the enter ability and Azra can protect itself")
    void ninjutsuAzraCanProtectItself() {
        Permanent attacker = addCreatureReady(player1, new UniversalAutomaton());
        addCreatureReady(player2, new UniversalAutomaton());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.setHand(player1, List.of(new AzraSmokeshaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.assertInHand(player1, "Universal Automaton");
        harness.assertNotOnBattlefield(player1, "Universal Automaton");
        harness.assertNotOnBattlefield(player1, "Azra Smokeshaper");
        harness.passBothPriorities();
        Permanent azra = findPermanent(player1, "Azra Smokeshaper");
        harness.handlePermanentChosen(player1, azra.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, azra, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(azra.isTapped()).isTrue();
        assertThat(azra.isAttacking()).isTrue();
        assertThat(azra.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("An unaffordable ninjutsu activation does not return the attacker")
    void insufficientManaDoesNotReturnAttacker() {
        Permanent attacker = addCreatureReady(player1, new UniversalAutomaton());
        addCreatureReady(player2, new UniversalAutomaton());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.setHand(player1, List.of(new AzraSmokeshaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Universal Automaton");
        harness.assertInHand(player1, "Azra Smokeshaper");
        assertThat(attacker.isAttacking()).isTrue();
    }
}
