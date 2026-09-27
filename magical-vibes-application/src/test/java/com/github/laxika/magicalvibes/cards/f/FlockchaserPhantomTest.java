package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlockchaserPhantom.class, GrizzlyBears.class, SolRing.class})
class FlockchaserPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives the next spell convoke and consumes the grant")
    void attackingGrantsConvokeToNextSpell() {
        Permanent phantom = addCreatureReady(player1, new FlockchaserPhantom());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(phantom)));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID creatureId = creature.getId();
        harness.setHand(player1, List.of(new SolRing()));
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creatureId));

        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        creature.untap();
        harness.setHand(player1, List.of(new SolRing()));
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
