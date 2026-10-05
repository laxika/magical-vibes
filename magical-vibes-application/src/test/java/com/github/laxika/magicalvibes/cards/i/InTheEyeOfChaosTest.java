package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.h.HeadlessHorseman;
import com.github.laxika.magicalvibes.cards.u.Urborg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InTheEyeOfChaos.class, Boomerang.class, HeadlessHorseman.class, Urborg.class})
class InTheEyeOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant when its caster cannot pay its mana value")
    void countersInstantWhenCasterCannotPayManaValue() {
        harness.addToBattlefield(player1, new InTheEyeOfChaos());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "In the Eye of Chaos"));

        harness.assertInGraveyard(player2, "Boomerang");
        harness.assertOnBattlefield(player1, "In the Eye of Chaos");
    }

    @Test
    @DisplayName("Lets an instant resolve when its caster pays its mana value")
    void letsInstantResolveWhenCasterPaysManaValue() {
        harness.addToBattlefield(player1, new InTheEyeOfChaos());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "In the Eye of Chaos"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertInHand(player1, "In the Eye of Chaos");
        harness.assertInGraveyard(player2, "Boomerang");
    }

    @Test
    @DisplayName("Counters an instant cast by its controller when the controller cannot pay")
    void countersInstantCastByItsControllerWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new InTheEyeOfChaos());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "In the Eye of Chaos"));

        harness.assertInGraveyard(player1, "Boomerang");
        harness.assertOnBattlefield(player1, "In the Eye of Chaos");
    }

    @Test
    @DisplayName("Counters an instant when its caster declines the mana value payment")
    void countersInstantWhenCasterDeclinesManaValuePayment() {
        harness.addToBattlefield(player1, new InTheEyeOfChaos());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "In the Eye of Chaos"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Boomerang");
        harness.assertOnBattlefield(player1, "In the Eye of Chaos");
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new InTheEyeOfChaos());
        harness.setHand(player2, List.of(new HeadlessHorseman()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Headless Horseman");
    }

    @Test
    @DisplayName("Allows mana abilities during resolution to supply the tax payment")
    void allowsManaAbilitiesToSupplyTaxPayment() {
        harness.addToBattlefield(player1, new InTheEyeOfChaos());
        harness.addToBattlefield(player2, new Urborg());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "In the Eye of Chaos"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "In the Eye of Chaos");
        harness.assertInGraveyard(player2, "Boomerang");
    }

    @Test
    @DisplayName("A newly resolved copy replaces the older world enchantment across controllers")
    void newlyResolvedCopyReplacesOlderWorldEnchantment() {
        harness.addToBattlefield(player1, new InTheEyeOfChaos());
        harness.setHand(player2, List.of(new InTheEyeOfChaos()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "In the Eye of Chaos");
        harness.assertNotOnBattlefield(player1, "In the Eye of Chaos");
        harness.assertOnBattlefield(player2, "In the Eye of Chaos");
    }
}
