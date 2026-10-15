package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InTheBrave.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class, LiquimetalCoating.class})
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

    @Test
    void opposingArtifactsAndOrdinaryPermanentsDoNotMeetTheThreshold() {
        Permanent oin = harness.enterBattlefieldAndReturn(player1, new InTheBrave());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, oin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, oin, Keyword.HASTE)).isFalse();
    }

    @Test
    void gainsTheBonusWhenEnteringWithTwoArtifactsAlreadyPresent() {
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        Permanent oin = harness.enterBattlefieldAndReturn(player1, new InTheBrave());

        assertThat(gqs.getEffectivePower(gd, oin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, oin, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSickWithoutAnEnduringStory() {
        Permanent oin = harness.enterBattlefieldAndReturn(player1, new InTheBrave());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(oin.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enduringStoryAllowsImmediateActivationAndDiscardingALand() {
        Permanent oin = harness.enterBattlefieldAndReturn(player1, new InTheBrave());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(oin.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateWithAnEmptyHand() {
        Permanent oin = addReadyOin();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(oin.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent oin = addReadyOin();
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(oin.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent oin = addReadyOin();
        oin.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainsAnEnduringStoryWhenAnExistingPermanentBecomesAnArtifact() {
        harness.enterBattlefieldAndReturn(player1, new LiquimetalCoating());
        Permanent oin = harness.enterBattlefieldAndReturn(player1, new InTheBrave());
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, oin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, oin, Keyword.HASTE)).isFalse();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, oin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, oin, Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyOin() {
        return addCreatureReady(player1, new InTheBrave());
    }
}
