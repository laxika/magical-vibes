package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchersOfQarsi.class, AvenSurveyor.class, FeralKrushok.class})
class ArchersOfQarsiTest extends BaseCardTest {

    @Test
    @DisplayName("Archers of Qarsi can't attack")
    void cannotAttack() {
        Permanent archers = addCreatureReady(player1, new ArchersOfQarsi());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(archers))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Archers of Qarsi can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent archers = addCreatureReady(player2, new ArchersOfQarsi());
        Permanent surveyor = addCreatureReady(player1, new AvenSurveyor());
        surveyor.setAttacking(true);

        prepareDeclareBlockers(player1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(archers);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(surveyor);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(archers.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Archers of Qarsi can block a creature without flying")
    void canBlockGroundCreature() {
        Permanent archers = addCreatureReady(player2, new ArchersOfQarsi());
        Permanent krushok = addCreatureReady(player1, new FeralKrushok());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(krushok)));
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(archers);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(krushok);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(archers.isBlocking()).isTrue();
    }
}
