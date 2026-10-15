package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurnBurnTreeAndFern.class, FireDiamond.class, WallOfStone.class})
class BurnBurnTreeAndFernTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I deals 6 damage to a creature an opponent controls")
    void chapterIDealsDamageToOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WallOfStone());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new WallOfStone());
        castSaga();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
    }

    @Test
    @DisplayName("Chapter II destroys an artifact an opponent controls")
    void chapterIIDestroysOpponentsArtifact() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FireDiamond());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new FireDiamond());
        Permanent saga = addSagaWithLore(1);

        advanceToChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(opponentArtifact.getId())
                .doesNotContain(ownArtifact.getId());

        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact);
        assertThat(saga).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Chapter III adds one red mana")
    void chapterIIIAddsRedMana() {
        Permanent saga = addSagaWithLore(2);

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("Chapter IV adds one red mana, then sacrifices the Saga")
    void chapterIVAddsRedManaAndSacrificesSaga() {
        Permanent saga = addSagaWithLore(3);

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    @DisplayName("Chapter I cannot damage a target that is no longer opponent-controlled")
    void chapterIRechecksTargetController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfStone());
        castSaga();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Chapter II cannot destroy a target that is no longer opponent-controlled")
    void chapterIIRechecksTargetController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FireDiamond());
        addSagaWithLore(1);
        advanceToChapter();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertNotInGraveyard(player2, "Fire Diamond");
    }

    @Test
    @DisplayName("The final chapter uses the stack and the Saga remains until it resolves")
    void finalChapterWaitsForResolution() {
        Permanent saga = addSagaWithLore(3);
        advanceToChapter();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        harness.assertInGraveyard(player1, "Burn, Burn, Tree and Fern");
    }

    private void castSaga() {
        harness.castFromHand(player1, new BurnBurnTreeAndFern(), "{3}{R}");
        harness.passBothPriorities();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BurnBurnTreeAndFern());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
