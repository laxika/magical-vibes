package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.w.WizardsStaff;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriKeeperOfSongs.class, FountainOfYouth.class, WizardsStaff.class, Humble.class})
class OriKeeperOfSongsTest extends BaseCardTest {

    @Test
    void doesNotGetTheEnduringStoryBonusBeforeThreshold() {
        Permanent ori = harness.addToBattlefieldAndReturn(player1, new OriKeeperOfSongs());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, ori)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ori)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ori, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void getsBonusAndVigilanceAfterEnduringStoryAndKeepsThem() {
        Permanent ori = harness.addToBattlefieldAndReturn(player1, new OriKeeperOfSongs());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, ori)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ori)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ori, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Fountain of Youth"));

        assertThat(gqs.getEffectivePower(gd, ori)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ori, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void opposingArtifactsDoNotCountTowardYourStory() {
        Permanent ori = harness.enterBattlefieldAndReturn(player1, new OriKeeperOfSongs());
        harness.enterBattlefieldAndReturn(player1, new WizardsStaff());
        harness.enterBattlefieldAndReturn(player2, new WizardsStaff());
        harness.enterBattlefieldAndReturn(player2, new WizardsStaff());

        assertThat(gqs.getEffectivePower(gd, ori)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ori, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.playersWithEnduringStory).doesNotContain(player1.getId(), player2.getId());
    }

    @Test
    void enteringOriCountsItselfAndAttacksWithoutTappingAfterThreshold() {
        harness.enterBattlefieldAndReturn(player1, new WizardsStaff());
        harness.enterBattlefieldAndReturn(player1, new WizardsStaff());
        Permanent ori = harness.enterBattlefieldAndReturn(player1, new OriKeeperOfSongs());
        ori.setSummoningSick(false);

        assertThat(gqs.getEffectivePower(gd, ori)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ori)).isEqualTo(3);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(2)));

        assertThat(ori.isTapped()).isFalse();
    }

    @Test
    void gainsEnduringStoryWhenStoriedReturnsAtCleanup() {
        Permanent ori = harness.enterBattlefieldAndReturn(player1, new OriKeeperOfSongs());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, ori.getId());

        harness.enterBattlefieldAndReturn(player1, new WizardsStaff());
        harness.enterBattlefieldAndReturn(player1, new WizardsStaff());
        assertThat(gd.playersWithEnduringStory).doesNotContain(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, ori)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ori)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ori, Keyword.VIGILANCE)).isTrue();
    }
}
