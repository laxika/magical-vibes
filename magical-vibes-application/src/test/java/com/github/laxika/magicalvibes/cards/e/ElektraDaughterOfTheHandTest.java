package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElektraDaughterOfTheHand.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class ElektraDaughterOfTheHandTest extends BaseCardTest {

    @Test
    void destroysTargetCreatureAnOpponentControlsWithPowerThreeOrLess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void cannotTargetCreatureWithPowerGreaterThanThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    void cannotTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void sneakReturnsUnblockedAttackerAndEntersTappedAndAttackingAtTheSamePlayer() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, target.getId(), null, List.of(), List.of(), false,
                null, null, List.of(attacker.getId()));
        harness.passBothPriorities();

        Permanent elektra = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ElektraDaughterOfTheHand)
                .findFirst()
                .orElseThrow();
        assertThat(elektra.isTapped()).isTrue();
        assertThat(elektra.isAttacking()).isTrue();
        assertThat(elektra.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Hill Giant");
    }
}
