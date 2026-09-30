package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuturistSpellthief.class, Shock.class, GrizzlyBears.class})
class FuturistSpellthiefTest extends BaseCardTest {

    @Test
    void conjuresDuplicateOfTargetSpellWithPerpetualAnyColorCasting() {
        Shock shock = new Shock();
        castSpellthiefTargeting(shock);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(shock.getId());
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(Card::isTokenCard)
                .findFirst()
                .orElseThrow();
        assertThat(duplicate.isTokenCard()).isTrue();
        assertThat(gd.perpetualAnyColorManaForCastCardIds).contains(duplicate.getId());
    }

    @Test
    void canCastConjuredDuplicateWithOffColorMana() {
        Shock shock = new Shock();
        castSpellthiefTargeting(shock);
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        int duplicateIndex = findConjuredDuplicateIndex(player1);
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, duplicateIndex, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void cannotTargetPermanent() {
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FuturistSpellthief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a spell");
    }

    private void castSpellthiefTargeting(Shock shock) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new FuturistSpellthief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castCreature(player1, 0, shock.getId());
        harness.passBothPriorities();
    }

    private int findConjuredDuplicateIndex(Player player) {
        List<Card> hand = gd.playerHands.get(player.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isTokenCard()) {
                return i;
            }
        }
        throw new AssertionError("Conjured duplicate not found in hand");
    }
}
