package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarRoku;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLegendOfRoku.class, AvatarRoku.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, Mountain.class})
class TheLegendOfRokuTest extends BaseCardTest {

    @Test
    void chapterOneExilesTopThreeCardsForPlay() {
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card third = new HillGiant();
        Card remaining = new Mountain();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
    }

    @Test
    void chapterTwoAddsOneManaOfChosenColor() {
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void chapterThreeTransformsIntoAvatarRoku() {
        addSaga(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent avatar = findPermanent(player1, "Avatar Roku");
        assertThat(avatar.isTransformed()).isTrue();
        harness.assertNotOnBattlefield(player1, "The Legend of Roku");
    }

    @Test
    void avatarRokuFirebendingAddsFourRedManaUntilCombatEnds() {
        Permanent avatar = addTransformedAvatar();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(avatar.isTapped()).isTrue();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void avatarRokuCreatesFirebendingDragonToken() {
        Permanent avatar = addTransformedAvatar();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getCard().getPower()).isEqualTo(4);
        assertThat(dragon.getCard().getToughness()).isEqualTo(4);
        dragon.setSummoningSick(false);

        declareAttackers(List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(avatar.isTapped()).isFalse();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheLegendOfRoku());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedAvatar() {
        TheLegendOfRoku front = new TheLegendOfRoku();
        Permanent avatar = new Permanent(front);
        avatar.setCard(front.getBackFaceCard());
        avatar.setTransformed(true);
        avatar.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(avatar);
        return avatar;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
