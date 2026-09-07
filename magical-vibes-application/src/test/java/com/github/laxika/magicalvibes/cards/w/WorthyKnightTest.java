package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorthyKnight.class, VenerableKnight.class, GrizzlyBears.class})
class WorthyKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Knight creates a 1/1 Human token")
    void castingKnightCreatesHumanToken() {
        harness.addToBattlefield(player1, new WorthyKnight());
        harness.setHand(player1, List.of(new VenerableKnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Human");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-Knight creature does not create a Human token")
    void castingNonKnightDoesNotCreateHumanToken() {
        harness.addToBattlefield(player1, new WorthyKnight());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human")).isZero();
    }

    @Test
    @DisplayName("An opponent casting a Knight does not create a Human token")
    void opponentCastingKnightDoesNotCreateHumanToken() {
        harness.addToBattlefield(player1, new WorthyKnight());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VenerableKnight()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human")).isZero();
    }
}
