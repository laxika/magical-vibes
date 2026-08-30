package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarlovWatchdog.class, GrizzlyBears.class, MasterOfPearls.class})
class KarlovWatchdogTest extends BaseCardTest {

    @Test
    void boostsYourCreaturesWhenYouAttackWithThreeOrMoreCreatures() {
        Permanent watchdog = addCreatureReady(player1, new KarlovWatchdog());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent thirdAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watchdog)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, thirdAttacker)).isEqualTo(3);
    }

    @Test
    void doesNotBoostWhenFewerThanThreeCreaturesAttack() {
        Permanent watchdog = addCreatureReady(player1, new KarlovWatchdog());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watchdog)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent watchdog = addCreatureReady(player1, new KarlovWatchdog());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, watchdog)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watchdog)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(2);
    }

    @Test
    void preventsOpponentFromTurningFaceDownPermanentFaceUpDuringYourTurn() {
        addCreatureReady(player1, new KarlovWatchdog());
        Permanent faceDown = addFaceDownMasterOfPearls(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.turnFaceUp(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(faceDown)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be turned face up");
        assertThat(faceDown.isFaceDown()).isTrue();
    }

    @Test
    void allowsOpponentToTurnFaceDownPermanentFaceUpDuringTheirTurn() {
        addCreatureReady(player1, new KarlovWatchdog());
        Permanent faceDown = addFaceDownMasterOfPearls(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(faceDown));

        assertThat(faceDown.isFaceDown()).isFalse();
    }

    @Test
    void doesNotPreventYouFromTurningYourOwnPermanentFaceUp() {
        addCreatureReady(player1, new KarlovWatchdog());
        Permanent faceDown = addFaceDownMasterOfPearls(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(faceDown));

        assertThat(faceDown.isFaceDown()).isFalse();
    }

    private Permanent addFaceDownMasterOfPearls(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MasterOfPearls());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }
}
