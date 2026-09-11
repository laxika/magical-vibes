package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InTheBrave.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class})
class InTheBraveTest extends BaseCardTest {

    @Test
    void doesNotGetTheEnduringStoryBonusBeforeThreshold() {
        Permanent oin = harness.enterBattlefieldAndReturn(player1, new InTheBrave());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, oin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, oin, Keyword.HASTE)).isFalse();
    }

    @Test
    void getsTheEnduringStoryBonusAndKeepsIt() {
        Permanent oin = harness.enterBattlefieldAndReturn(player1, new InTheBrave());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, oin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, oin, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Fountain of Youth"));

        assertThat(gqs.getEffectivePower(gd, oin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, oin, Keyword.HASTE)).isTrue();
    }

    @Test
    void paysManaAndDiscardCostToDrawACard() {
        Permanent oin = addReadyOin();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(oin.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    private Permanent addReadyOin() {
        return addCreatureReady(player1, new InTheBrave());
    }
}
