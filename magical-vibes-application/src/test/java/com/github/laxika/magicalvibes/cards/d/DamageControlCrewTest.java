package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DamageControlCrew.class, HillGiant.class, GrizzlyBears.class, FountainOfYouth.class, BadMoon.class})
class DamageControlCrewTest extends BaseCardTest {

    @Test
    @DisplayName("Repair returns a card with mana value 4 or greater from the graveyard to hand")
    void repairReturnsExpensiveCardToHand() {
        Card target = new HillGiant();
        Card tooCheap = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, tooCheap));
        castWithRepair();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Repair cannot target a card with mana value less than 4")
    void repairCannotTargetCheapCard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castWithRepair();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Impound exiles a target artifact")
    void impoundExilesArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castWithImpound(target.getId());

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Impound exiles a target enchantment")
    void impoundExilesEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BadMoon());
        castWithImpound(target.getId());

        harness.assertNotOnBattlefield(player2, "Bad Moon");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Impound cannot target a creature")
    void impoundCannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castWithImpound(target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithRepair() {
        harness.setHand(player1, List.of(new DamageControlCrew()));
        addMana();
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castWithImpound(UUID targetId) {
        harness.setHand(player1, List.of(new DamageControlCrew()));
        addMana();
        harness.castCreature(player1, 0, 1, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
