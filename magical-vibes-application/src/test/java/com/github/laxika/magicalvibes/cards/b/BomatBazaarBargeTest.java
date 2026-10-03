package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BomatBazaarBarge.class, GrizzlyBears.class, SerraAngel.class})
class BomatBazaarBargeTest extends BaseCardTest {

    @Test
    void enteringBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new BomatBazaarBarge()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    void crewAnimatesBargeAndTapsCrew() {
        Permanent barge = addBargeReady(player1);
        Permanent crew = addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(barge.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, barge)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addBargeReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addBargeReady(Player player) {
        return addCreatureReady(player, new BomatBazaarBarge());
    }

    @Test
    void multipleCreaturesCanPayCrewCost() {
        Permanent barge = addBargeReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, barge)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, barge)).isTrue();
        assertThat(barge.isTapped()).isFalse();
    }

    @Test
    void summoningSickCreatureCanCrewSummoningSickBarge() {
        Permanent barge = harness.addToBattlefieldAndReturn(player1, new BomatBazaarBarge());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        barge.setSummoningSick(true);
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, barge)).isTrue();
        assertThat(barge.isSummoningSick()).isTrue();
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        addBargeReady(player1);
        Permanent crew = addCreatureReady(player1, new SerraAngel());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void opponentsCreatureCannotPayCrewCost() {
        addBargeReady(player1);
        Permanent crew = addCreatureReady(player2, new SerraAngel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(crew.isTapped()).isFalse();
    }

    @Test
    void crewAnimationEndsAtCleanup() {
        Permanent barge = addBargeReady(player1);
        addCreatureReady(player1, new SerraAngel());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.isCreature(gd, barge)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, barge)).isFalse();
        assertThat(barge.isAnimatedUntilEndOfTurn()).isFalse();
    }
}
