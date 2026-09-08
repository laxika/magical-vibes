package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheKingpinOfCrime.class, GiantSpider.class, GoblinPiker.class, GrizzlyBears.class})
class TheKingpinOfCrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying after attacking makes qualifying creatures assign toughness-based combat damage")
    void payingAfterAttackingUsesToughnessForQualifyingCreatures() {
        Permanent kingpin = addReadyCreature(player1, new TheKingpinOfCrime());
        Permanent spider = addReadyCreature(player1, new GiantSpider());
        Permanent piker = addReadyCreature(player1, new GoblinPiker());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, kingpin)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the attack payment leaves combat damage unchanged")
    void decliningAttackPaymentDoesNothing() {
        addReadyCreature(player1, new TheKingpinOfCrime());
        Permanent spider = addReadyCreature(player1, new GiantSpider());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack payment effect expires at end of turn")
    void attackPaymentEffectExpiresAtEndOfTurn() {
        addReadyCreature(player1, new TheKingpinOfCrime());
        Permanent spider = addReadyCreature(player1, new GiantSpider());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Kingpin's extort drains an opponent when a spell is cast")
    void extortDrainsOpponent() {
        harness.addToBattlefield(player1, new TheKingpinOfCrime());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
