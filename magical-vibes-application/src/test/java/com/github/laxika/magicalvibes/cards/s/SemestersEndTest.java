package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AgelessGuardian;
import com.github.laxika.magicalvibes.cards.k.KasminaEnigmaSage;
import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SemestersEnd.class, AgelessGuardian.class, KasminaEnigmaSage.class, LetterOfAcceptance.class})
class SemestersEndTest extends BaseCardTest {

    @Test
    @DisplayName("Returns selected creatures and planeswalkers with their appropriate counters")
    void returnsSelectedPermanentsWithAppropriateCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new KasminaEnigmaSage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 1);
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), planeswalker.getId()));

        harness.assertNotOnBattlefield(player1, "Ageless Guardian");
        harness.assertNotOnBattlefield(player1, "Kasmina, Enigma Sage");

        advanceToEndStep();

        Permanent returnedCreature = findPermanent(player1, "Ageless Guardian");
        Permanent returnedPlaneswalker = findPermanent(player1, "Kasmina, Enigma Sage");
        assertThat(returnedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returnedPlaneswalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returnedPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target only creatures and planeswalkers you control")
    void cannotTargetPermanentAnOpponentControls() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AgelessGuardian());
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker you control");
    }

    @Test
    void canResolveWithoutChoosingTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SemestersEnd);
    }

    @Test
    void resetsOldCountersAndLeavesUnselectedCreaturesAlone() {
        Permanent selected = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        Permanent unselected = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        selected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        unselected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, List.of(selected.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(unselected);
        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).contains(unselected);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != unselected).findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(selected.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unselected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void returnsPermanentsWithDifferentOwnersInOneResolution() {
        Permanent owned = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, List.of(owned.getId(), borrowed.getId()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Ageless Guardian")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player2, "Ageless Guardian")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void skipsTargetThatChangesControllerBeforeResolution() {
        Permanent legal = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        Permanent changed = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, List.of(legal.getId(), changed.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(changed);
        gd.playerBattlefields.get(player2.getId()).add(changed);
        gd.stolenCreatures.put(changed.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(changed);
        advanceToEndStep();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(changed);
        assertThat(changed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Ageless Guardian")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetAnArtifactThatIsNeitherCreatureNorPlaneswalker() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LetterOfAcceptance());
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker you control");
    }

    @Test
    void castingDuringEndStepWaitsForTheFollowingEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AgelessGuardian());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.castAndResolveInstant(player1, 0, List.of(creature.getId())));

        harness.assertNotOnBattlefield(player1, "Ageless Guardian");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Ageless Guardian");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ageless Guardian")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
