package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bramblesnap.class, NestInvader.class, Forest.class})
class BramblesnapTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another creature you control gives Bramblesnap +1/+1")
    void tappingAnotherCreatureBoostsSelf() {
        Permanent bramblesnap = addReady(player1, new Bramblesnap());
        Permanent bears = addReady(player1, new NestInvader());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(bramblesnap);
        harness.activateAbility(player1, idx, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bramblesnap.getPowerModifier()).isEqualTo(1);
        assertThat(bramblesnap.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can tap itself to pay the cost")
    void canTapItself() {
        Permanent bramblesnap = addReady(player1, new Bramblesnap());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(bramblesnap);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(bramblesnap.isTapped()).isTrue();
        assertThat(bramblesnap.getPowerModifier()).isEqualTo(1);
        assertThat(bramblesnap.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bramblesnap = addReady(player1, new Bramblesnap());
        Permanent bears = addReady(player1, new NestInvader());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(bramblesnap);
        harness.activateAbility(player1, idx, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bramblesnap.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bramblesnap.getPowerModifier()).isEqualTo(0);
        assertThat(bramblesnap.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate with no untapped creature to tap")
    void cannotActivateWithNoUntappedCreature() {
        Permanent bramblesnap = addReady(player1, new Bramblesnap());
        bramblesnap.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(bramblesnap);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning-sick Bramblesnap can tap itself because the cost has no tap symbol")
    void summoningSickSourceCanTapItself() {
        Permanent bramblesnap = harness.addToBattlefieldAndReturn(player1, new Bramblesnap());
        bramblesnap.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(bramblesnap.isTapped()).isTrue();
        assertThat(bramblesnap.getPowerModifier()).isZero();
        assertThat(bramblesnap.getToughnessModifier()).isZero();

        harness.passBothPriorities();

        assertThat(bramblesnap.getPowerModifier()).isEqualTo(1);
        assertThat(bramblesnap.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Bramblesnap can tap a summoning-sick creature to pay its cost")
    void tappedSourceCanTapSummoningSickCreature() {
        Permanent bramblesnap = addReady(player1, new Bramblesnap());
        bramblesnap.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(bramblesnap.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(bramblesnap.getPowerModifier()).isEqualTo(1);
        assertThat(bramblesnap.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations stack and cannot reuse a tapped creature")
    void repeatedActivationsAccumulateBoosts() {
        Permanent bramblesnap = addReady(player1, new Bramblesnap());
        Permanent creature = addReady(player1, new NestInvader());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bramblesnap.getId());
        harness.activateAbility(player1, 0, null, null);

        assertThat(bramblesnap.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(bramblesnap.getPowerModifier()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(bramblesnap.getPowerModifier()).isEqualTo(1);
        assertThat(bramblesnap.getToughnessModifier()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(bramblesnap.getPowerModifier()).isEqualTo(2);
        assertThat(bramblesnap.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's untapped creature cannot pay the cost")
    void opponentsCreatureCannotPayCost() {
        Permanent bramblesnap = addReady(player1, new Bramblesnap());
        bramblesnap.tap();
        Permanent opponentCreature = addReady(player2, new NestInvader());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(bramblesnap.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("An untapped noncreature permanent cannot pay the cost")
    void noncreatureCannotPayCost() {
        Permanent bramblesnap = addReady(player1, new Bramblesnap());
        bramblesnap.tap();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(forest.isTapped()).isFalse();
        assertThat(bramblesnap.getPowerModifier()).isZero();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
