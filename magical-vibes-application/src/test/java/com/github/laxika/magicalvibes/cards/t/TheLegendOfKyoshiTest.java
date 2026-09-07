package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarKyoshi;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLegendOfKyoshi.class, AvatarKyoshi.class, Forest.class, GrizzlyBears.class,
        HillGiant.class})
class TheLegendOfKyoshiTest extends BaseCardTest {

    @Test
    void chapterOneDrawsCardsEqualToGreatestControlledCreaturePower() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new HillGiant());
        addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void chapterTwoEarthbendsForCardsInHandAndAddsIsland() {
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSaga(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    void chapterThreeReturnsTheSagaTransformed() {
        addSaga(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent avatar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
        assertThat(avatar.getCard()).isInstanceOf(AvatarKyoshi.class);
    }

    @Test
    void avatarKyoshiGrantsKeywordsToControlledLandsAndAddsGreatestPowerMana() {
        Permanent avatar = addTransformedAvatar(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        assertThat(gqs.hasKeyword(gd, forest, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HEXPROOF)).isTrue();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(avatar), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheLegendOfKyoshi());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedAvatar(Player player) {
        TheLegendOfKyoshi front = new TheLegendOfKyoshi();
        Permanent avatar = new Permanent(front);
        avatar.setCard(front.getBackFaceCard());
        avatar.setTransformed(true);
        avatar.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(avatar);
        return avatar;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
