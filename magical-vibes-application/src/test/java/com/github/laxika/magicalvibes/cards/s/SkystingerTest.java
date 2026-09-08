package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skystinger.class, SuntailHawk.class, GrizzlyBears.class})
class SkystingerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature with flying triggers +5/+0 boost")
    void blockingFlyingCreatureTriggersBoost() {
        Permanent skystinger = addReadySkystinger(player2);
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skystinger)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, skystinger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocking a creature without flying does not trigger boost")
    void blockingNonFlyingCreatureDoesNotTrigger() {
        Permanent skystinger = addReadySkystinger(player2);
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, skystinger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent skystinger = addReadySkystinger(player2);
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(skystinger.getPowerModifier()).isEqualTo(5);

        harness.forceStep(TurnStep.CLEANUP);
        skystinger.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, skystinger)).isEqualTo(3);
    }

    private Permanent addReadySkystinger(Player player) {
        Permanent perm = new Permanent(new Skystinger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
