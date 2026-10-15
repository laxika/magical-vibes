package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KangeeSkyWarden.class, AirElemental.class, GrizzlyBears.class, SuntailHawk.class})
class KangeeSkyWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures with flying get +2/+0")
    void boostsAttackingCreaturesWithFlying() {
        Permanent kangee = addCreatureReady(player1, new KangeeSkyWarden());
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        Permanent groundCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent stayingBack = addCreatureReady(player1, new SuntailHawk());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(kangee.getEffectivePower()).isEqualTo(5);
        assertThat(kangee.getEffectiveToughness()).isEqualTo(3);
        assertThat(flyer.getEffectivePower()).isEqualTo(6);
        assertThat(flyer.getEffectiveToughness()).isEqualTo(4);
        assertThat(groundCreature.getEffectivePower()).isEqualTo(2);
        assertThat(groundCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(stayingBack.getEffectivePower()).isEqualTo(1);
        assertThat(stayingBack.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking creatures with flying get +0/+2")
    void boostsBlockingCreaturesWithFlying() {
        Permanent kangee = addCreatureReady(player1, new KangeeSkyWarden());
        Permanent flyer = addCreatureReady(player1, new SuntailHawk());
        Permanent groundBlocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent flyingAttacker = addCreatureReady(player2, new AirElemental());
        Permanent groundAttacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player1.getId()).indexOf(kangee),
                        gd.playerBattlefields.get(player2.getId()).indexOf(groundAttacker)),
                new BlockerAssignment(gd.playerBattlefields.get(player1.getId()).indexOf(flyer),
                        gd.playerBattlefields.get(player2.getId()).indexOf(flyingAttacker)),
                new BlockerAssignment(gd.playerBattlefields.get(player1.getId()).indexOf(groundBlocker),
                        gd.playerBattlefields.get(player2.getId()).indexOf(groundAttacker))
        ));
        harness.passBothPriorities();

        assertThat(kangee.getEffectivePower()).isEqualTo(3);
        assertThat(kangee.getEffectiveToughness()).isEqualTo(5);
        assertThat(flyer.getEffectivePower()).isEqualTo(1);
        assertThat(flyer.getEffectiveToughness()).isEqualTo(3);
        assertThat(groundBlocker.getEffectivePower()).isEqualTo(2);
        assertThat(groundBlocker.getEffectiveToughness()).isEqualTo(2);
        assertThat(flyingAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(flyingAttacker.getEffectiveToughness()).isEqualTo(4);
    }
}
