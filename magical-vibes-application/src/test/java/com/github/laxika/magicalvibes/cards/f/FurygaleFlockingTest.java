package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FurygaleFlocking.class, Shock.class})
class FurygaleFlockingTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery cards in the graveyard reduce the spell's generic cost")
    void graveyardCardsReduceCost() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new FurygaleFlocking()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Creates two flying hasty Elementals for each opponent")
    void createsElementalsForEachOpponent() {
        harness.setHand(player1, List.of(new FurygaleFlocking()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Elemental").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
            assertThat(token.isMustAttackThisTurn()).isTrue();
            assertThat(token.getMustAttackTargetId()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("The tokens must attack their assigned opponent if able")
    void tokensMustAttackAssignedOpponent() {
        harness.setHand(player1, List.of(new FurygaleFlocking()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Haste and the attack requirement end with the turn")
    void temporaryAbilitiesWearOff() {
        harness.setHand(player1, List.of(new FurygaleFlocking()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Elemental").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
        assertThat(token.isMustAttackThisTurn()).isFalse();
        assertThat(token.getMustAttackTargetId()).isNull();
    }
}
