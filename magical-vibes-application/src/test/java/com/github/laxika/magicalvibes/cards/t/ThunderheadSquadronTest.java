package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
import com.github.laxika.magicalvibes.cards.s.SkyclaveAerialist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderheadSquadron.class, CopperHostCrusher.class, SkyclaveAerialist.class})
class ThunderheadSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke lets a creature spell use a creature to pay generic mana")
    void convokePaysGenericMana() {
        Permanent convokingCreature = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        harness.setHand(player1, List.of(new ThunderheadSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokingCreature.getId()));

        assertThat(convokingCreature.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thunderhead Squadron");
    }

    @Test
    @DisplayName("Flying prevents ground creatures from blocking but permits flying blockers")
    void hasFlying() {
        Permanent squadron = harness.addToBattlefieldAndReturn(player1, new ThunderheadSquadron());

        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new CopperHostCrusher());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new SkyclaveAerialist());

        assertThat(bls.canBlockAttacker(gd, groundBlocker, squadron,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, squadron,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void summoningSickBlueCreatureCanConvokeBlueMana() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new SkyclaveAerialist());
        convoker.setSummoningSick(true);
        harness.setHand(player1, List.of(new ThunderheadSquadron()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thunderhead Squadron");
    }

    @Test
    void greenCreatureCannotConvokeBlueMana() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new CopperHostCrusher());
        harness.setHand(player1, List.of(new ThunderheadSquadron()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convoker.getId()))).isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Thunderhead Squadron");
        assertThat(convoker.isTapped()).isFalse();
    }

    @Test
    void tappedCreatureCannotConvoke() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new SkyclaveAerialist());
        convoker.tap();
        harness.setHand(player1, List.of(new ThunderheadSquadron()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convoker.getId()))).isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Thunderhead Squadron");
    }

    @Test
    void opponentCreatureCannotConvoke() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player2, new SkyclaveAerialist());
        harness.setHand(player1, List.of(new ThunderheadSquadron()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convoker.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
        harness.assertInHand(player1, "Thunderhead Squadron");
    }

    @Test
    void sameCreatureCannotConvokeTwice() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new SkyclaveAerialist());
        harness.setHand(player1, List.of(new ThunderheadSquadron()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convoker.getId(), convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
        harness.assertInHand(player1, "Thunderhead Squadron");
    }
}
