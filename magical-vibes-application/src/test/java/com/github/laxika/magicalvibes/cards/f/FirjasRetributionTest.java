package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.PoisonTheCup;
import com.github.laxika.magicalvibes.cards.s.StalwartValkyrie;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirjasRetribution.class, StalwartValkyrie.class, BeskirShieldmate.class})
class FirjasRetributionTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a flying, vigilant Angel Warrior")
    void chapterICreatesAngelWarrior() {
        Permanent saga = addSagaWithLore(0);

        triggerAndResolveChapter(saga);

        Permanent token = findPermanent(player1, "Angel Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ANGEL, CardSubtype.WARRIOR);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Chapter II grants Angels a power-restricted destruction ability")
    void chapterIIGrantsPowerRestrictedDestruction() {
        Permanent saga = addSagaWithLore(1);
        Permanent angel = addCreatureReady(player1, new StalwartValkyrie());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        Permanent equalPowerTarget = addCreatureReady(player2, new StalwartValkyrie());

        triggerAndResolveChapter(saga);

        int angelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(angel);
        assertThatThrownBy(() -> harness.activateAbility(player1, angelIndex, null, equalPowerTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power less than this creature's power");

        harness.activateAbility(player1, angelIndex, null, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Beskir Shieldmate");
        harness.assertOnBattlefield(player2, "Stalwart Valkyrie");
    }

    @Test
    @DisplayName("Chapter III grants double strike to Angels until end of turn")
    void chapterIIIGrantsDoubleStrikeUntilEndOfTurn() {
        Permanent saga = addSagaWithLore(2);
        Permanent angel = addCreatureReady(player1, new StalwartValkyrie());
        Permanent nonAngel = addCreatureReady(player1, new BeskirShieldmate());

        triggerAndResolveChapter(saga);

        assertThat(angel.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(nonAngel.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void chapterIIAbilityExpiresAtEndOfTurn() {
        Permanent saga = addSagaWithLore(1);
        Permanent angel = addCreatureReady(player1, new StalwartValkyrie());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        triggerAndResolveChapter(saga);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(angel), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Beskir Shieldmate");
    }

    @Test
    void chapterIIDoesNotGrantAbilityToNonAngelsOrLaterAngels() {
        Permanent saga = addSagaWithLore(1);
        Permanent nonAngel = addCreatureReady(player1, new BeskirShieldmate());
        nonAngel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingAngel = addCreatureReady(player2, new StalwartValkyrie());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        triggerAndResolveChapter(saga);
        Permanent lateAngel = addCreatureReady(player1, new StalwartValkyrie());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(nonAngel), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lateAngel), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(opposingAngel), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chapterIIRechecksTargetPowerAtResolution() {
        Permanent saga = addSagaWithLore(1);
        Permanent angel = addCreatureReady(player1, new StalwartValkyrie());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        triggerAndResolveChapter(saga);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(angel), null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Beskir Shieldmate");
        harness.assertNotInGraveyard(player2, "Beskir Shieldmate");
        assertThat(angel.isTapped()).isTrue();
    }

    @Test
    void chapterIIRechecksAngelPowerAtResolution() {
        Permanent saga = addSagaWithLore(1);
        Permanent angel = addCreatureReady(player1, new StalwartValkyrie());
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        triggerAndResolveChapter(saga);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(angel), null, target.getId());
        angel.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Beskir Shieldmate");
        harness.assertNotInGraveyard(player2, "Beskir Shieldmate");
    }

    @Test
    @CardUsed({PoisonTheCup.class})
    void chapterIIUsesLastKnownPowerAfterAngelDies() {
        Permanent saga = addSagaWithLore(1);
        Permanent angel = addCreatureReady(player1, new StalwartValkyrie());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new StalwartValkyrie());
        triggerAndResolveChapter(saga);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(angel), null, target.getId());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new PoisonTheCup()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, angel.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Stalwart Valkyrie");
        harness.assertInGraveyard(player2, "Stalwart Valkyrie");
    }

    @Test
    void chapterIIICoversOnlyAngelsControlledAtResolution() {
        Permanent saga = addSagaWithLore(2);
        Permanent angel = addCreatureReady(player1, new StalwartValkyrie());
        Permanent opposingAngel = addCreatureReady(player2, new StalwartValkyrie());
        triggerAndResolveChapter(saga);
        Permanent lateAngel = addCreatureReady(player1, new StalwartValkyrie());

        assertThat(gqs.hasKeyword(gd, angel, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingAngel, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateAngel, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Firja's Retribution");
    }

    @Test
    @CardUsed({Opalescence.class, MaskwoodNexus.class})
    void chapterIIIncludesTheSagaWhenItIsAnAngelCreature() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent saga = addSagaWithLore(1);
        saga.setSummoningSick(false);
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        triggerAndResolveChapter(saga);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(saga), null, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Beskir Shieldmate");
    }

    @Test
    @CardUsed({Opalescence.class, MaskwoodNexus.class})
    void chapterIIIIncludesTheSagaWhenItIsAnAngelCreature() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent saga = addSagaWithLore(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passBothPriorities();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.stack).isNotEmpty();
        saga.setCounterCount(CounterType.LORE, 2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Firja's Retribution");
        assertThat(gqs.hasKeyword(gd, saga, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void castingSagaTriggersChapterIOnEntry() {
        harness.castFromHand(player1, new FirjasRetribution(), "{1}{W}{W}{B}");
        resolveAllTriggers();

        Permanent saga = findPermanent(player1, "Firja's Retribution");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Angel Warrior")).isEqualTo(1);
        assertThat(countPermanents(player2, "Angel Warrior")).isZero();
    }

    @Test
    void grantedTapAbilityStillRequiresAnAngelWithoutSummoningSickness() {
        Permanent saga = addSagaWithLore(1);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new StalwartValkyrie());
        angel.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new BeskirShieldmate());
        triggerAndResolveChapter(saga);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(angel), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Beskir Shieldmate");
        assertThat(angel.isTapped()).isFalse();
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FirjasRetribution());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void triggerAndResolveChapter(Permanent saga) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(saga.getCounterCount(CounterType.LORE)).isGreaterThan(0);
        resolveAllTriggers();
    }
}
