package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrestonGarveyMinuteman.class, Forest.class, AbundantGrowth.class})
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
        Permanent preston = harness.addToBattlefieldAndReturn(player1, new PrestonGarveyMinuteman());
        preston.setSummoningSick(false);
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

    private void advanceToCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
