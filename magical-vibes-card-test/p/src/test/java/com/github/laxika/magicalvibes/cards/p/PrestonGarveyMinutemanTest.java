package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StrongBack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrestonGarveyMinuteman.class, Forest.class, AbundantGrowth.class, StrongBack.class})
class PrestonGarveyMinutemanTest extends BaseCardTest {

    @Test
    void beginningOfCombatCreatesSettlementAttachedToTargetLand() {
        harness.addToBattlefield(player1, new PrestonGarveyMinuteman());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        Permanent settlement = findPermanent(player1, "Settlement");
        assertThat(settlement.getCard().isToken()).isTrue();
        assertThat(settlement.getCard().isAura()).isTrue();
        assertThat(settlement.getAttachedTo()).isEqualTo(land.getId());
    }

    @Test
    void settlementLetsEnchantedLandProduceAnyColor() {
        harness.addToBattlefield(player1, new PrestonGarveyMinuteman());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void attackingUntapsEachEnchantedPermanentYouControl() {
        addCreatureReady(player1, new PrestonGarveyMinuteman());
        Permanent enchantedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent unenchantedLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AbundantGrowth());
        aura.setAttachedTo(enchantedLand.getId());
        enchantedLand.tap();
        unenchantedLand.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(enchantedLand.isTapped()).isFalse();
        assertThat(unenchantedLand.isTapped()).isTrue();
    }

    @Test
    void settlementCanOnlyTargetALandYouControl() {
        Permanent preston = harness.addToBattlefieldAndReturn(player1, new PrestonGarveyMinuteman());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownLand.getId(), player1.getId())
                .doesNotContain(preston.getId(), opposingLand.getId());
        harness.handlePermanentChosen(player1, ownLand.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Settlement").getAttachedTo()).isEqualTo(ownLand.getId());
    }

    @Test
    void choosingNoTargetDoesNotCreateSettlement() {
        harness.addToBattlefield(player1, new PrestonGarveyMinuteman());
        harness.addToBattlefield(player1, new Forest());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Settlement")).isZero();
    }

    @Test
    void noLegalLandDoesNotCreateSettlement() {
        harness.addToBattlefield(player1, new PrestonGarveyMinuteman());
        harness.addToBattlefield(player2, new Forest());

        advanceToCombat(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Settlement")).isZero();
    }

    @Test
    void beginningOfOpponentsCombatDoesNotCreateSettlement() {
        harness.addToBattlefield(player1, new PrestonGarveyMinuteman());
        harness.addToBattlefield(player1, new Forest());

        advanceToCombat(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Settlement")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackUntapsEnchantedPrestonWithoutRemovingItFromCombat() {
        Permanent preston = addCreatureReady(player1, new PrestonGarveyMinuteman());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrongBack());
        aura.setAttachedTo(preston.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });

        assertThat(preston.isTapped()).isFalse();
        assertThat(preston.isAttacking()).isTrue();
    }

    @Test
    void attackUntapsOwnLandEnchantedByOpponentsAuraButNotOpponentsLand() {
        addCreatureReady(player1, new PrestonGarveyMinuteman());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new AbundantGrowth());
        opposingAura.setAttachedTo(ownLand.getId());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new AbundantGrowth());
        ownAura.setAttachedTo(opposingLand.getId());
        ownLand.tap();
        opposingLand.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isTrue();
    }

    private void advanceToCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
