package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitsunesTechnique.class, GrizzlyBears.class})
class KitsunesTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Normally mills half of an opponent's library rounded up")
    void millsHalfLibraryRoundedUp() {
        harness.setLibrary(player2, library(11));
        castNormally(player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        harness.assertInGraveyard(player1, "Kitsune's Technique");
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and still mills the targeted opponent")
    void sneaksAndMillsOpponent() {
        Permanent attacker = addCreatureReady();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setLibrary(player2, library(10));
        harness.setHand(player1, List.of(new KitsunesTechnique()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantWithAlternateCost(player1, 0, player2.getId(), List.of(attacker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new KitsunesTechnique()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castNormally(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new KitsunesTechnique()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private Permanent addCreatureReady() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        return attacker;
    }

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
