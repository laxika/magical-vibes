package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BriarHydra.class, Forest.class, Island.class, Mountain.class, GrizzlyBears.class})
class BriarHydraTest extends BaseCardTest {

    private Permanent addReady(Permanent permanent) {
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Combat damage puts one counter per distinct basic land type on a creature you control")
    void combatDamageUsesDomainAndTargetsOwnCreature() {
        Permanent hydra = addReady(new Permanent(new BriarHydra()));
        Permanent target = addReady(new Permanent(new GrizzlyBears()));
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        opponentCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Forest()));
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Island()));
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Mountain()));
        hydra.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID targetId = target.getId();
        assertThat(choice.validIds()).contains(targetId).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage with no basic land types puts no counters")
    void combatDamageWithNoDomainPutsNoCounters() {
        Permanent hydra = addReady(new Permanent(new BriarHydra()));
        Permanent target = addReady(new Permanent(new GrizzlyBears()));
        hydra.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
