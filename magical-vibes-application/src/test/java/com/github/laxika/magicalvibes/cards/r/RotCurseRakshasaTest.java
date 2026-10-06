package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DeltaBloodflies;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RotCurseRakshasa.class, GrizzlyBears.class, DeltaBloodflies.class})
class RotCurseRakshasaTest extends BaseCardTest {

    @Test
    @DisplayName("Renew puts decayed counters on X target creatures and exiles the card")
    void renewPutsDecayedCountersOnTargetCreatures() {
        RotCurseRakshasa rakshasa = new RotCurseRakshasa();
        harness.setGraveyard(player1, List.of(rakshasa));
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.activateGraveyardAbility(gd, player1, 0, 0, 2, null, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.DECAYED)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.DECAYED)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, com.github.laxika.magicalvibes.model.Keyword.DECAYED)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(rakshasa.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(rakshasa.getId()));
    }

    @Test
    @DisplayName("A decayed Rot-Curse Rakshasa cannot block and is sacrificed after attacking")
    void decayedCreatureCannotBlockAndIsSacrificedAfterAttacking() {
        Permanent rakshasa = addCreatureReady(player1, new RotCurseRakshasa());

        assertThat(bls.canBlock(gd, rakshasa)).isFalse();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(rakshasa.getId()));
    }

    @Test
    void renewRequiresExactlyXTargets() {
        RotCurseRakshasa source = new RotCurseRakshasa();
        harness.setGraveyard(player1, List.of(source));
        Permanent target = addCreatureReady(player2, new RotCurseRakshasa());
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> gs.activateGraveyardAbility(gd, player1, 0, 0, 2,
                null, List.of(target.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
        assertThat(target.getCounterCount(CounterType.DECAYED)).isZero();
    }

    @Test
    void renewCanActivateWithZeroTargetsAndExilesSourceAsCost() {
        RotCurseRakshasa source = new RotCurseRakshasa();
        harness.setGraveyard(player1, List.of(source));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.activateGraveyardAbility(gd, player1, 0, 0, 0, null, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(source);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        harness.passBothPriorities();
    }

    @Test
    void renewRejectsDuplicateTargets() {
        RotCurseRakshasa source = new RotCurseRakshasa();
        harness.setGraveyard(player1, List.of(source));
        Permanent target = addCreatureReady(player2, new RotCurseRakshasa());
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> gs.activateGraveyardAbility(gd, player1, 0, 0, 2,
                null, List.of(target.getId(), target.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
    }

    @Test
    void renewCannotActivateOutsideMainPhase() {
        RotCurseRakshasa source = new RotCurseRakshasa();
        harness.setGraveyard(player1, List.of(source));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> gs.activateGraveyardAbility(gd, player1, 0, 0, 0,
                null, List.of())).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
    }

    @Test
    void renewCannotActivateDuringOpponentsTurn() {
        RotCurseRakshasa source = new RotCurseRakshasa();
        harness.setGraveyard(player1, List.of(source));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> gs.activateGraveyardAbility(gd, player1, 0, 0, 0,
                null, List.of())).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
    }

    @Test
    void renewCannotActivateWithNonemptyStack() {
        harness.setGraveyard(player1, List.of(new RotCurseRakshasa(), new RotCurseRakshasa()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        gs.activateGraveyardAbility(gd, player1, 0, 0, 0, null, List.of());

        assertThatThrownBy(() -> gs.activateGraveyardAbility(gd, player1, 0, 0, 0,
                null, List.of())).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void renewedCreatureWithNativeAttackTriggerIsStillSacrificed() {
        harness.setGraveyard(player1, List.of(new RotCurseRakshasa()));
        Permanent target = addCreatureReady(player1, new DeltaBloodflies());
        harness.addMana(player1, ManaColor.BLACK, 3);
        gs.activateGraveyardAbility(gd, player1, 0, 0, 1, null, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.DECAYED)).isEqualTo(1);
        assertThat(bls.canBlock(gd, target)).isFalse();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Delta Bloodflies");
    }

    @Test
    void trampleDealsExcessDamageThroughBlocker() {
        addCreatureReady(player1, new RotCurseRakshasa());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void renewRejectsPlayerAsTarget() {
        RotCurseRakshasa source = new RotCurseRakshasa();
        harness.setGraveyard(player1, List.of(source));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> gs.activateGraveyardAbility(gd, player1, 0, 0, 1,
                null, List.of(player2.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
    }
}
