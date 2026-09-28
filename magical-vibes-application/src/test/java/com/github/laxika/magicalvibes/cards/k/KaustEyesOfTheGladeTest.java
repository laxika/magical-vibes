package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaustEyesOfTheGlade.class, GrizzlyBears.class})
class KaustEyesOfTheGladeTest extends BaseCardTest {

    @Test
    void turnsAFaceDownAttackingCreatureYouControlFaceUp() {
        Permanent kaust = addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent attacker = castFaceDownBear();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(indexOf(attacker)));
            harness.activateAbility(player1, indexOf(kaust), null, attacker.getId());
            harness.passBothPriorities();
        });

        assertThat(attacker.isFaceDown()).isFalse();
    }

    @Test
    void drawsOnlyForCreaturesTurnedFaceUpThisTurn() {
        Permanent kaust = addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent turnedUpAttacker = castFaceDownBear();
        Permanent faceDownAttacker = castFaceDownBear();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(indexOf(turnedUpAttacker), indexOf(faceDownAttacker)));
            harness.activateAbility(player1, indexOf(kaust), null, turnedUpAttacker.getId());
            harness.passBothPriorities();
        });

        resolveCombat();
        resolveAllTriggers();

        assertThat(turnedUpAttacker.isFaceDown()).isFalse();
        assertThat(faceDownAttacker.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotTargetANonAttackingCreature() {
        Permanent kaust = addCreatureReady(player1, new KaustEyesOfTheGlade());
        Permanent target = castFaceDownBear();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(kaust), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castFaceDownBear() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.addMorph("{0}");
        harness.setHand(player1, List.of(bear));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .reduce((first, second) -> second)
                .orElseThrow();
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
