package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarYangchen;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLegendOfYangchen.class, AvatarYangchen.class, RuneclawBear.class, LilianaVess.class,
        LightningBolt.class})
class TheLegendOfYangchenTest extends BaseCardTest {

    @Test
    void chapterIExilesUpToOneQualifyingPermanentPerPlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        target.setCounterCount(CounterType.LOYALTY, target.getCard().getLoyalty());
        Permanent tooSmall = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent saga = addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(tooSmall.getOriginalCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIIDrawsThreeForTargetOpponentAndControllerWhenAccepted() {
        Permanent saga = addSaga(1);
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt(),
                new LightningBolt()));
        harness.setLibrary(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        int player1HandBefore = gd.playerHands.get(player1.getId()).size();
        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandBefore + 3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIIITransformsTheSaga() {
        addSaga(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent avatar = findPermanent(player1, "Avatar Yangchen");
        assertThat(avatar.isTransformed()).isTrue();
        harness.assertNotOnBattlefield(player1, "The Legend of Yangchen");
    }

    @Test
    void avatarYangchenAirbendsAnotherPermanentOnSecondSpell() {
        addTransformedSaga();
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void chapterIAllowsOpponentToExileTheSagaAfterBothPlayersChoose() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        target.setCounterCount(CounterType.LOYALTY, target.getCard().getLoyalty());
        Permanent saga = addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(saga.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(saga.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void chapterIIDrawsNothingWhenDeclined() {
        addSaga(1);
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt(),
                new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        int controllerHand = gd.playerHands.get(player1.getId()).size();
        int opponentHand = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHand);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHand);
    }

    @Test
    void chapterIIIReturnsUnderAbilityControllersControlRatherThanOwners() {
        Permanent saga = addSaga(2);
        saga.getOriginalCard().setOwnerId(player2.getId());

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar Yangchen");
        harness.assertNotOnBattlefield(player2, "Avatar Yangchen");
        assertThat(findPermanent(player1, "Avatar Yangchen").isTransformed()).isTrue();
    }
    @Test
    void avatarTriggersOnlyOnSecondSpellAndAllowsNoTargetOnOpponentsTurn() {
        addTransformedSaga();
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void airbentCardCanBeCastByItsOwnerForTwoGenericMana() {
        addTransformedSaga();
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        target.getOriginalCard().setOwnerId(player2.getId());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 2);
        harness.castFromExile(player2, target.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
    }
    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheLegendOfYangchen());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedSaga() {
        TheLegendOfYangchen front = new TheLegendOfYangchen();
        Permanent saga = new Permanent(front);
        saga.setCard(front.getBackFaceCard());
        saga.setTransformed(true);
        saga.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(saga);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
