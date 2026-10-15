package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DispellingExhale.class, DragonWhelp.class, GrizzlyBears.class})
class DispellingExhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when a Dragon was beheld from the battlefield and its controller cannot pay {4}")
    void beheldDragonFromBattlefieldUsesFourManaTax() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());
        GrizzlyBears targetSpell = castTargetSpellWithMana(3);
        harness.setHand(player2, List.of(new DispellingExhale()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(dragon.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a spell when a Dragon was beheld from hand and its controller cannot pay {4}")
    void beheldDragonFromHandUsesFourManaTax() {
        GrizzlyBears targetSpell = castTargetSpellWithMana(3);
        DragonWhelp dragon = new DragonWhelp();
        harness.setHand(player2, List.of(new DispellingExhale(), dragon));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(), List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without behold, the target spell survives when its controller pays {2}")
    void withoutBeholdUsesTwoManaTax() {
        GrizzlyBears targetSpell = castTargetSpellWithMana(2);
        harness.setHand(player2, List.of(new DispellingExhale()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without behold, counters the spell when its controller cannot pay {2}")
    void withoutBeholdCountersWhenPaymentIsUnaffordable() {
        GrizzlyBears targetSpell = castTargetSpellWithMana(1);
        prepareExhale();

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without behold, the controller may decline to pay even with enough mana")
    void decliningPaymentCountersSpell() {
        GrizzlyBears targetSpell = castTargetSpellWithMana(2);
        prepareExhale();

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A beheld Dragon increases the tax to {4}, which can be paid")
    void beheldDragonAllowsFourManaPayment() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());
        GrizzlyBears targetSpell = castTargetSpellWithMana(4);
        prepareExhale();

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(dragon.getId()), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Dragon Whelp");
    }

    @Test
    @DisplayName("Controlling a Dragon does not increase the tax if behold is declined")
    void availableDragonDoesNotAutomaticallyIncreaseTax() {
        harness.addToBattlefield(player2, new DragonWhelp());
        GrizzlyBears targetSpell = castTargetSpellWithMana(2);
        prepareExhale();

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The {4} tax persists after the beheld Dragon leaves the battlefield")
    void beholdIsRememberedAfterDragonLeaves() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());
        GrizzlyBears targetSpell = castTargetSpellWithMana(3);
        prepareExhale();

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(dragon.getId()), List.of());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dragon));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Dragon Whelp");
    }

    private void prepareExhale() {
        harness.setHand(player2, List.of(new DispellingExhale()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }

    private GrizzlyBears castTargetSpellWithMana(int extraMana) {
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.castFromHand(player1, targetSpell, "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, extraMana);
        harness.passPriority(player1);
        return targetSpell;
    }
}
