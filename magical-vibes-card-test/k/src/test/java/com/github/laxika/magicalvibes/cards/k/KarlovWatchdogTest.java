package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.cards.s.ShowstoppingSurprise;
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

@CardUsed({KarlovWatchdog.class, GrizzlyBears.class, MasterOfPearls.class, DogWalker.class, ShowstoppingSurprise.class})
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

    @Test
    void boostsNonattackingCreaturesAndDoesNotBoostOpponents() {
        Permanent watchdog = addCreatureReady(player1, new KarlovWatchdog());
        Permanent first = addCreatureReady(player1, new DogWalker());
        Permanent second = addCreatureReady(player1, new DogWalker());
        Permanent third = addCreatureReady(player1, new DogWalker());
        Permanent opponent = addCreatureReady(player2, new DogWalker());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, watchdog)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, watchdog)).isEqualTo(3);
        for (Permanent attacker : List.of(first, second, third)) {
            assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        }
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(1);
    }

    @Test
    void stillBoostsAfterAnAttackerAndWatchdogLeaveBeforeResolution() {
        Permanent watchdog = addCreatureReady(player1, new KarlovWatchdog());
        Permanent first = addCreatureReady(player1, new DogWalker());
        Permanent second = addCreatureReady(player1, new DogWalker());
        Permanent third = addCreatureReady(player1, new DogWalker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1, 2, 3)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(watchdog);
        gd.playerBattlefields.get(player1.getId()).remove(third);
        gd.playerGraveyards.get(player1.getId()).add(watchdog.getCard());
        gd.playerGraveyards.get(player1.getId()).add(third.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void allowsOpponentToTurnFaceUpAfterWatchdogLeaves() {
        Permanent watchdog = addCreatureReady(player1, new KarlovWatchdog());
        Permanent faceDown = addFaceDownMasterOfPearls(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 2);
        gd.playerBattlefields.get(player1.getId()).remove(watchdog);
        gd.playerGraveyards.get(player1.getId()).add(watchdog.getCard());

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(faceDown));

        assertThat(faceDown.isFaceDown()).isFalse();
    }

    @Test
    void preventsSpellFromTurningOpponentsCreatureFaceUpDuringYourTurn() {
        addCreatureReady(player1, new KarlovWatchdog());
        Permanent faceDown = addFaceDownMasterOfPearls(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ShowstoppingSurprise()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castInstant(player2, 0, faceDown.getId());
        resolveAllTriggers();

        assertThat(faceDown.isFaceDown()).isTrue();
    }

    private Permanent addFaceDownMasterOfPearls(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MasterOfPearls());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }
}
