package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({SelfcraftMechan.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class SelfcraftMechanTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact puts a counter on a target creature and draws a card")
    void sacrificeArtifactCountersCreatureAndDrawsCard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelfcraftMechan()));
        harness.setLibrary(player1, List.of(new Forest()));
        castSelfcraftMechan();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the artifact sacrifice does nothing")
    void declineSacrifice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelfcraftMechan()));
        harness.setLibrary(player1, List.of(new Forest()));
        castSelfcraftMechan();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only an artifact can be sacrificed for the ability")
    void nonArtifactCannotBeSacrificed() {
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelfcraftMechan()));
        harness.setLibrary(player1, List.of(new Forest()));
        castSelfcraftMechan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonArtifact);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castSelfcraftMechan() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
