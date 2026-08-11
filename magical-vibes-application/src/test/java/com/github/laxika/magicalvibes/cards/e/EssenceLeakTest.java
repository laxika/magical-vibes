package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EssenceLeakTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay sacrifices an enchanted red permanent")
    void decliningSacrificesRedPermanent() {
        enchantOpponent(new RagingGoblin());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Paying the enchanted green permanent's mana cost keeps it on the battlefield")
    void payingManaCostKeepsGreenPermanent() {
        enchantOpponent(new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The enchanted permanent's colored mana cost is required")
    void coloredManaCostIsRequired() {
        enchantOpponent(new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An off-color enchanted permanent gets no upkeep ability")
    void offColorPermanentIsUnaffected() {
        enchantOpponent(new FountainOfYouth());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    private Permanent enchantOpponent(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, card);
        Permanent aura = new Permanent(new EssenceLeak());
        aura.setAttachedTo(permanent.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return permanent;
    }
}
