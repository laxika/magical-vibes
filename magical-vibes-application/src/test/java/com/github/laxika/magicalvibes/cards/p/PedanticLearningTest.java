package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PedanticLearningTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {1} to draw when a land is put into the graveyard from the library")
    void paysToDrawWhenLandIsMilled() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllChoices(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Declining the payment does not draw")
    void decliningPaymentDoesNotDraw() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player1, new Millstone());
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), grizzlyBears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllChoices(false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(grizzlyBears);
    }

    @Test
    @DisplayName("A land entering the graveyard from the battlefield does not trigger")
    void battlefieldLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Forest"));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isNotEmpty();
    }

    private void resolveAllChoices(boolean acceptPayment) {
        int guard = 0;
        while ((!gd.stack.isEmpty() || gd.interaction.activeInteraction() != null) && guard++ < 50) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, acceptPayment);
            } else {
                harness.passBothPriorities();
            }
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
