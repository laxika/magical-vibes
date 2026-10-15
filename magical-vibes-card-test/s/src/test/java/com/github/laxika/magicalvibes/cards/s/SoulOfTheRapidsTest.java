package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrashingTide;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulOfTheRapids.class, RaptorCompanion.class, CrashingTide.class})
class SoulOfTheRapidsTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        Permanent soul = addCreatureReady(player1, new SoulOfTheRapids());
        Permanent raptor = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        assertThat(bls.canBlockAttacker(gd, raptor, soul,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void canBeBlockedByAnotherFlyingCreatureDespiteHexproof() {
        Permanent attacker = addCreatureReady(player1, new SoulOfTheRapids());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SoulOfTheRapids());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void flyingCreatureCanBlockCreatureWithoutFlying() {
        Permanent raptor = addCreatureReady(player1, new RaptorCompanion());
        Permanent soul = harness.addToBattlefieldAndReturn(player2, new SoulOfTheRapids());

        assertThat(bls.canBlockAttacker(gd, soul, raptor,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void opponentCannotTargetSoul() {
        Permanent soul = harness.addToBattlefieldAndReturn(player1, new SoulOfTheRapids());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new CrashingTide()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0, soul.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Soul of the Rapids");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanTargetAndReturnSoulToHand() {
        SoulOfTheRapids card = new SoulOfTheRapids();
        Permanent soul = harness.addToBattlefieldAndReturn(player1, card);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CrashingTide()));
        harness.setLibrary(player1, List.of(new RaptorCompanion()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0, soul.getId());

        harness.assertNotOnBattlefield(player1, "Soul of the Rapids");
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
    }
}
