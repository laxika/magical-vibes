package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.n.NoDachi;
import com.github.laxika.magicalvibes.cards.o.OrochiLeafcaller;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({UnnaturalSpeed.class, NoDachi.class, OrochiLeafcaller.class})
class UnnaturalSpeedTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants target creature haste")
    void resolvingGrantsHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrochiLeafcaller());
        harness.setHand(player1, List.of(new UnnaturalSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can grant haste to a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrochiLeafcaller());
        harness.setHand(player1, List.of(new UnnaturalSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOff() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrochiLeafcaller());
        harness.setHand(player1, List.of(new UnnaturalSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new OrochiLeafcaller());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new NoDachi());
        harness.setHand(player1, List.of(new UnnaturalSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrochiLeafcaller());
        harness.setHand(player1, List.of(new UnnaturalSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Only the targeted creature gains haste")
    void onlyTargetedCreatureGainsHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrochiLeafcaller());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new OrochiLeafcaller());
        harness.setHand(player1, List.of(new UnnaturalSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste remains during the end step")
    void hasteRemainsDuringEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrochiLeafcaller());
        harness.setHand(player1, List.of(new UnnaturalSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A creature with summoning sickness can attack after gaining haste")
    void hasteAllowsNewCreatureToAttack() {
        harness.forceActivePlayer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrochiLeafcaller());
        target.setSummoningSick(true);
        harness.setHand(player1, List.of(new UnnaturalSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(target.isAttacking()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }
}
