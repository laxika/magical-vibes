package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.f.FesterhideBoar;
import com.github.laxika.magicalvibes.cards.c.CurseOfDeathsHold;
import com.github.laxika.magicalvibes.cards.d.DearlyDeparted;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MentorOfTheMeek.class, AvacynsPilgrim.class, DarkthicketWolf.class,
        FesterhideBoar.class, CurseOfDeathsHold.class, DearlyDeparted.class,
        IntangibleVirtue.class, MidnightHaunting.class})
class MentorOfTheMeekTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} draws a card when creature with power 2 or less enters")
    void payingOneManaDrawsCard() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        // Power 2 should trigger.
        harness.setHand(player1, List.of(new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // for Mentor's {1} cost
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Darkthicket Wolf
        harness.passBothPriorities(); // Resolve MayPayManaEffect from stack -> may prompt

        // Accept and pay {1} to draw a card.
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining does not draw a card")
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        harness.setHand(player1, List.of(new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Darkthicket Wolf
        harness.passBothPriorities(); // Resolve MayPayManaEffect from stack -> may prompt

        // Decline
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when creature with power greater than 2 enters")
    void doesNotTriggerForHighPowerCreature() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        // Power 3 should not trigger.
        harness.setHand(player1, List.of(new FesterhideBoar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Festerhide Boar

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for itself entering the battlefield")
    void doesNotTriggerForItself() {
        // Mentor does not trigger for itself.
        harness.setHand(player1, List.of(new MentorOfTheMeek()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Mentor

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when opponent's creature with power 2 or less enters")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.setHand(player1, List.of());

        // An opponent's creature should not trigger Mentor.
        harness.enterBattlefieldAndReturn(player2, new DarkthicketWolf());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers for creature with power less than 2")
    void triggersForPowerOneCreature() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        // Power 1 should trigger.
        harness.setHand(player1, List.of(new AvacynsPilgrim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // for Mentor's {1} cost
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Avacyn's Pilgrim
        harness.passBothPriorities(); // Resolve MayPayManaEffect from stack -> may prompt

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Entering counters that raise power above two prevent the trigger")
    void enteringCountersPreventTrigger() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.setGraveyard(player1, List.of(new DearlyDeparted(), new DearlyDeparted()));

        var pilgrim = harness.enterBattlefieldAndReturn(player1, new AvacynsPilgrim());

        assertThat(gqs.getEffectivePower(gd, pilgrim)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Static bonuses that raise entering token power above two prevent the trigger")
    void staticBonusesPreventTrigger() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.setHand(player1, List.of(new MidnightHaunting()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Static penalties reducing entering power to two allow the trigger")
    void staticPenaltyAllowsTrigger() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.addToBattlefieldAndReturn(player2, new CurseOfDeathsHold())
                .setAttachedTo(player1.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        var boar = harness.enterBattlefieldAndReturn(player1, new FesterhideBoar());

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Raising power after entry does not stop an existing trigger")
    void powerIncreaseAfterEntryDoesNotStopDraw() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AvacynsPilgrim()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        var wolf = harness.enterBattlefieldAndReturn(player1, new DarkthicketWolf());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each entering token triggers separately and requires its own payment")
    void tokensRequireSeparatePayments() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.setHand(player1, List.of(new MidnightHaunting()));
        harness.setLibrary(player1, List.of(new AvacynsPilgrim(), new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
