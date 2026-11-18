package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZealousDisplay.class, Forest.class, GrizzlyBears.class})
class ZealousDisplayTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts and untaps your creatures during an opponent's turn")
    void boostsAndUntapsOnOpponentsTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.tap();
        ownLand.tap();
        opponentCreature.tap();

        castOnOpponentsTurn();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, opponentCreature)).isZero();
    }

    @Test
    @DisplayName("Does not untap creatures during your turn")
    void doesNotUntapOnYourTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.tap();

        castOnYourTurn();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The power boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOnYourTurn();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ownCreature)).isZero();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts every creature present at resolution, whether tapped or untapped")
    void boostsAllCreaturesPresentAtResolution() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();

        castOnOpponentsTurn();

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(untappedCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, tappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, untappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untappedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringLater() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOnYourTurn();
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves without any creatures and leaves lands tapped")
    void resolvesWithoutCreatures() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        castOnOpponentsTurn();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ZealousDisplay);
    }

    private void castOnYourTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ZealousDisplay()));
        addMana();
        harness.castAndResolveInstant(player1, 0);
    }

    private void castOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ZealousDisplay()));
        addMana();
        harness.castAndResolveInstant(player1, 0);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
