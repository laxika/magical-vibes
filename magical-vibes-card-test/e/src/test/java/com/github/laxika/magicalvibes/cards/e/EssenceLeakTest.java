package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrakeSkullCameo;
import com.github.laxika.magicalvibes.cards.g.GoblinSpy;
import com.github.laxika.magicalvibes.cards.l.LlanowarElite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceLeak.class, DrakeSkullCameo.class, GoblinSpy.class, LlanowarElite.class})
class EssenceLeakTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay sacrifices an enchanted red permanent")
    void decliningSacrificesRedPermanent() {
        enchantOpponent(new GoblinSpy());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Goblin Spy");
    }

    @Test
    @DisplayName("Paying the enchanted green permanent's mana cost keeps it on the battlefield")
    void payingManaCostKeepsGreenPermanent() {
        enchantOpponent(new LlanowarElite());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Llanowar Elite");
    }

    @Test
    @DisplayName("The enchanted permanent's colored mana cost is required")
    void coloredManaCostIsRequired() {
        enchantOpponent(new LlanowarElite());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Llanowar Elite");
    }

    @Test
    @DisplayName("An off-color enchanted permanent gets no upkeep ability")
    void offColorPermanentIsUnaffected() {
        enchantOpponent(new DrakeSkullCameo());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Drake-Skull Cameo");
    }

    @Test
    @DisplayName("The Aura controller's upkeep does not trigger an opponent's enchanted permanent")
    void auraControllersUpkeepDoesNotTrigger() {
        enchantOpponent(new LlanowarElite());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Llanowar Elite");
    }

    @Test
    @DisplayName("Removing the Aura after upkeep begins does not remove the sacrifice trigger")
    void removingAuraDoesNotStopPendingTrigger() {
        enchantOpponent(new LlanowarElite());

        advanceToUpkeep(player2);
        Permanent aura = findPermanent(player1, "Essence Leak");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Llanowar Elite");
    }

    @Test
    @DisplayName("Paying the upkeep cost actually spends the enchanted permanent controller's mana")
    void paymentSpendsMana() {
        enchantOpponent(new LlanowarElite());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Llanowar Elite");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }
    private Permanent enchantOpponent(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, card);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EssenceLeak());
        aura.setAttachedTo(permanent.getId());
        return permanent;
    }
}
