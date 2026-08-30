package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarYangchen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TheLegendOfYangchen.class, AvatarYangchen.class, GrizzlyBears.class, LilianaVess.class,
        LightningBolt.class})
class TheLegendOfYangchenTest extends BaseCardTest {

    @Test
    void chapterIExilesUpToOneQualifyingPermanentPerPlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        target.setCounterCount(CounterType.LOYALTY, target.getCard().getLoyalty());
        Permanent tooSmall = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
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

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
