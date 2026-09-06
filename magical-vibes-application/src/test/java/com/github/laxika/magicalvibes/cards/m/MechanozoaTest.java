package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mechanozoa.class, GrizzlyBears.class, Millstone.class})
class MechanozoaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps an opponent's creature and puts a stun counter on it")
    void etbTapsAndStunsOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Mechanozoa()));
        addNormalMana();
        harness.castCreature(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB taps an opponent's noncreature artifact and puts a stun counter on it")
    void etbTapsAndStunsOpponentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        harness.setHand(player1, List.of(new Mechanozoa()));
        addNormalMana();
        harness.castCreature(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an artifact or creature you control")
    void cannotTargetOwnArtifactOrCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Mechanozoa()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Warp casts Mechanozoa for its alternate cost and exiles it at the next end step")
    void warpExilesAtNextEndStep() {
        Mechanozoa mechanozoa = new Mechanozoa();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(mechanozoa));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(mechanozoa.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(mechanozoa.getId())).isNotNull();
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
