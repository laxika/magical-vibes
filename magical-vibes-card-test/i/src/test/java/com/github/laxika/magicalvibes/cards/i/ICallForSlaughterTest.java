package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ICallForSlaughter.class, GrizzlyBears.class, Shock.class})
class ICallForSlaughterTest extends BaseCardTest {

    @Test
    void createsThreeHastyDevils() {
        setUpScheme();

        List<Permanent> devils = findPermanents(player1, "Devil");
        assertThat(devils).hasSize(3);
        assertThat(devils).allSatisfy(devil -> assertThat(devil.hasKeyword(Keyword.HASTE)).isTrue());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(devils).allSatisfy(devil -> assertThat(devil.hasKeyword(Keyword.HASTE)).isFalse());
    }

    @Test
    void controlledSourcesDealOneAdditionalDamageThisTurn() {
        setUpScheme();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void damageBonusAlsoAppliesToCombatAndExpires() {
        addCreatureReady(player1, new GrizzlyBears());
        setUpScheme();

        declareAttackers(List.of(0));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void eachDevilCanAttackImmediatelyAndDealsTwoDamage() {
        setUpScheme();

        declareAttackers(List.of(0, 1, 2));

        harness.assertLife(player2, 14);
    }

    @Test
    void opponentsSourcesDoNotReceiveTheDamageBonus() {
        setUpScheme();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void devilDeathDamageReceivesTheBonusEvenWhenTargetingItsController() {
        setUpScheme();
        Permanent devil = findPermanents(player1, "Devil").getFirst();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, devil.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Devil")).hasSize(2);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void devilDeathDamageCanKillACreature() {
        setUpScheme();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent devil = findPermanents(player1, "Devil").getFirst();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, devil.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void devilRetainsItsDeathAbilityAfterTheTurnEnds() {
        setUpScheme();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent devil = findPermanents(player1, "Devil").getFirst();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, devil.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private void setUpScheme() {
        Card sourceCard = new ICallForSlaughter();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                player1.getId(),
                sourceCard.getName() + "'s set-in-motion ability",
                sourceCard.getEffects(EffectSlot.SPELL),
                (UUID) null,
                (UUID) null));
        harness.passBothPriorities();
    }
}
