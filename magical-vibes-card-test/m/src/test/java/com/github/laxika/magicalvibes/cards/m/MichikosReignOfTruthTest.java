package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TouchTheSpiritRealm;
import com.github.laxika.magicalvibes.cards.e.EcologistsTerrarium;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.p.PortraitOfMichiko;
import com.github.laxika.magicalvibes.cards.s.SuitUp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MichikosReignOfTruth.class, PortraitOfMichiko.class, TouchTheSpiritRealm.class,
        EcologistsTerrarium.class, JukaiTrainee.class, SuitUp.class})
class MichikosReignOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I boosts a target creature for each artifact or enchantment controlled")
    void chapterIBoostsForArtifactsAndEnchantments() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        harness.addToBattlefield(player1, new EcologistsTerrarium());
        harness.addToBattlefield(player1, new TouchTheSpiritRealm());
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        harness.addToBattlefield(player1, new EcologistsTerrarium());
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
        harness.addToBattlefield(player1, new EcologistsTerrarium());
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

    @Test
    @DisplayName("Chapter I counts at resolution and its boost remains fixed afterwards")
    void chapterICountsAtResolutionAndKeepsThatBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        addSagaWithLore(0);
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.addToBattlefield(player1, new EcologistsTerrarium());
        harness.addToBattlefield(player2, new EcologistsTerrarium());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        harness.addToBattlefield(player1, new TouchTheSpiritRealm());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Portrait keeps its bonus after Suit Up sets its base power and toughness")
    void portraitBonusAppliesAfterBasePowerToughnessSetting() {
        harness.addToBattlefield(player1, new EcologistsTerrarium());
        addSagaWithLore(2);
        advanceToNextChapter();
        Permanent portrait = findPermanent(player1, "Portrait of Michiko");

        harness.setHand(player1, List.of(new SuitUp()));
        harness.setLibrary(player1, List.of(new JukaiTrainee()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, portrait.getId());

        assertThat(gqs.getEffectivePower(gd, portrait)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, portrait)).isEqualTo(7);
    }

    @Test
    @DisplayName("Portrait's bonus updates with its controller's permanents")
    void portraitBonusUpdatesDynamically() {
        addSagaWithLore(2);
        advanceToNextChapter();
        Permanent portrait = findPermanent(player1, "Portrait of Michiko");
        assertThat(gqs.getEffectivePower(gd, portrait)).isEqualTo(1);

        harness.addToBattlefield(player2, new EcologistsTerrarium());
        assertThat(gqs.getEffectivePower(gd, portrait)).isEqualTo(1);
        harness.addToBattlefield(player1, new EcologistsTerrarium());
        harness.addToBattlefield(player1, new TouchTheSpiritRealm());
        assertThat(gqs.getEffectivePower(gd, portrait)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, portrait)).isEqualTo(3);
    }
    @Test
    @DisplayName("Casting the Saga immediately triggers chapter I")
    void castingSagaTriggersFirstChapter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.castFromHand(player1, new MichikosReignOfTruth(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
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
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
