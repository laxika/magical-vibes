package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManOWar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DacksDuplicate.class, GrizzlyBears.class, ManOWar.class})
class DacksDuplicateTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature and can attack immediately with dethrone")
    void copiesCreatureWithHasteAndDethrone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new DacksDuplicate(), "{2}{U}{R}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent duplicate = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, duplicate)).isEqualTo(2);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(duplicate)));
        harness.passBothPriorities();

        assertThat(duplicate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the copy leaves a zero-toughness creature that dies")
    void decliningCopyDies() {
        harness.addToBattlefield(player2, new ManOWar());
        harness.castFromHand(player1, new DacksDuplicate(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Dack's Duplicate");
        harness.assertInGraveyard(player1, "Dack's Duplicate");
    }

    @Test
    @DisplayName("With no creatures to copy the Duplicate dies")
    void noCreaturesToCopyDies() {
        harness.castFromHand(player1, new DacksDuplicate(), "{2}{U}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dack's Duplicate");
        harness.assertInGraveyard(player1, "Dack's Duplicate");
    }

    @Test
    @DisplayName("Dethrone does not trigger against a player below the most life")
    void dethroneDoesNotTriggerAgainstLowerLife() {
        harness.setLife(player1, 21);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new DacksDuplicate(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        Permanent duplicate = findPermanent(player1, "Grizzly Bears");

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(duplicate)));
        harness.passBothPriorities();

        assertThat(duplicate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Copying a creature also copies its enters-the-battlefield ability")
    void copiesEnterAbility() {
        harness.addToBattlefield(player2, new ManOWar());
        harness.castFromHand(player1, new DacksDuplicate(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Man-o'-War"));
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Man-o'-War"));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Man-o'-War");
        harness.assertNotOnBattlefield(player2, "Man-o'-War");
        harness.assertOnBattlefield(player1, "Man-o'-War");
    }

    @Test
    @DisplayName("Copying another Duplicate retains its dethrone and adds a second instance")
    void copyingDuplicateTriggersDethroneTwice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new DacksDuplicate(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        Permanent firstCopy = findPermanent(player1, "Grizzly Bears");

        harness.castFromHand(player1, new DacksDuplicate(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopy.getId());
        Permanent secondCopy = findPermanents(player1, "Grizzly Bears").getLast();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(secondCopy)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(secondCopy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
