package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BanishingLight;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PortraitOfMichiko;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MichikosReignOfTruth.class, PortraitOfMichiko.class, BanishingLight.class,
        DarksteelRelic.class, GrizzlyBears.class})
class MichikosReignOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I boosts a target creature for each artifact or enchantment controlled")
    void chapterIBoostsForArtifactsAndEnchantments() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addToBattlefield(player1, new BanishingLight());
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Chapter II uses the current artifact and enchantment count")
    void chapterIIBoostsForCurrentCount() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DarksteelRelic());
        addSagaWithLore(1);

        advanceToNextChapter();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Chapter III returns the Saga transformed and the Portrait scales with permanents")
    void chapterIIITransformsIntoPortrait() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        Permanent saga = addSagaWithLore(2);

        advanceToNextChapter();

        Permanent portrait = findPermanent(player1, "Portrait of Michiko");
        assertThat(portrait).isNotSameAs(saga);
        assertThat(portrait.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, portrait)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, portrait)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III returns a stolen Saga under its controller's control")
    void stolenSagaReturnsUnderControllersControl() {
        MichikosReignOfTruth card = new MichikosReignOfTruth();
        card.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, card);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        Permanent portrait = findPermanent(player1, "Portrait of Michiko");
        assertThat(portrait.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new MichikosReignOfTruth());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
