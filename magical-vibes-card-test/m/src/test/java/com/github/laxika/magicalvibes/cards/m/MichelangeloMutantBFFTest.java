package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({MichelangeloMutantBFF.class, GrizzlyBears.class})
class MichelangeloMutantBFFTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Mutagen token")
    void etbCreatesMutagenToken() {
        harness.setHand(player1, List.of(new MichelangeloMutantBFF()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking creates a Mutagen token")
    void attackCreatesMutagenToken() {
        addCreatureReady(player1, new MichelangeloMutantBFF());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("A creature with a counter cannot be blocked by more than one creature")
    void counteredCreatureCannotBeBlockedByTwoCreatures() {
        Permanent michelangelo = addCreatureReady(player1, new MichelangeloMutantBFF());
        michelangelo.setCounterCount(CounterType.STUN, 1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        michelangelo.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("A creature without a counter can be blocked by two creatures")
    void uncounteredCreatureCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new MichelangeloMutantBFF());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void createdMutagenHasMutagenSubtype() {
        enterMichelangelo();

        assertThat(findPermanent(player1, "Mutagen").getCard().getSubtypes())
                .contains(CardSubtype.MUTAGEN);
    }

    @Test
    void mutagenPaysCostsBeforePuttingCounterOnOwnCreature() {
        Permanent creature = enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateMutagen(mutagen, creature);

        assertThat(mutagen.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenCanTargetOpponentCreature() {
        enterMichelangelo();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MichelangeloMutantBFF());
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateMutagen(mutagen, creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenCannotTargetNoncreatureArtifact() {
        enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activateMutagen(mutagen, mutagen))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
    }

    @Test
    void mutagenCannotActivateDuringOpponentTurn() {
        Permanent creature = enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> activateMutagen(mutagen, creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void mutagenCannotActivateOutsideMainPhase() {
        Permanent creature = enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> activateMutagen(mutagen, creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void mutagenCannotActivateWhileStackIsNonempty() {
        Permanent creature = enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> activateMutagen(mutagen, creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        resolveAllTriggers();
    }

    @Test
    void tappedMutagenCannotActivate() {
        Permanent creature = enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        mutagen.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activateMutagen(mutagen, creature))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mutagenCannotActivateWithoutMana() {
        Permanent creature = enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");

        assertThatThrownBy(() -> activateMutagen(mutagen, creature))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterOnAnotherControlledCreatureRestrictsBlocking() {
        addCreatureReady(player1, new MichelangeloMutantBFF());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    void counteredCreatureCanStillBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new MichelangeloMutantBFF());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentMichelangeloDoesNotRestrictBlockingOfYourCounteredCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        addCreatureReady(player2, new MichelangeloMutantBFF());
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent enterMichelangelo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new MichelangeloMutantBFF());
        resolveAllTriggers();
        return creature;
    }

    private void activateMutagen(Permanent mutagen, Permanent target) {
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), null, target.getId());
    }
}
