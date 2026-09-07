package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauntlessDismantler.class, GrizzlyBears.class, HowlingMine.class, Millstone.class, Ornithopter.class, RodOfRuin.class})
class DauntlessDismantlerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's artifacts enter tapped")
    void opponentsArtifactsEnterTapped() {
        harness.addToBattlefield(player1, new DauntlessDismantler());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent ornithopter = findPermanent(player2, "Ornithopter");
        assertThat(ornithopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's artifacts enter untapped")
    void controllersArtifactsEnterUntapped() {
        harness.addToBattlefield(player1, new DauntlessDismantler());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        assertThat(ornithopter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing it destroys artifacts with mana value X")
    void destroysArtifactsWithExactManaValue() {
        harness.addToBattlefield(player1, new DauntlessDismantler());
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dauntless Dismantler");
        harness.assertInGraveyard(player1, "Howling Mine");
        harness.assertInGraveyard(player2, "Millstone");
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Rod of Ruin");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
