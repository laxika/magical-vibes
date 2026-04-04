package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeadstrongBruteTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC CantBlockEffect and ControlsAnotherSubtypeConditionalEffect(PIRATE) wrapping GrantKeywordEffect(MENACE, SELF)")
    void hasCorrectStaticEffects() {
        HeadstrongBrute card = new HeadstrongBrute();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(CantBlockEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1))
                .isInstanceOf(ControlsAnotherSubtypeConditionalEffect.class);

        ControlsAnotherSubtypeConditionalEffect conditional =
                (ControlsAnotherSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(conditional.subtypes()).isEqualTo(Set.of(CardSubtype.PIRATE));
        assertThat(conditional.wrapped()).isInstanceOf(GrantKeywordEffect.class);

        GrantKeywordEffect grant = (GrantKeywordEffect) conditional.wrapped();
        assertThat(grant.keywords()).containsExactly(Keyword.MENACE);
        assertThat(grant.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Can't block =====

    @Test
    @DisplayName("Headstrong Brute cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent brute = new Permanent(new HeadstrongBrute());
        brute.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(brute);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    // ===== Conditional menace =====

    @Test
    @DisplayName("Has menace when controller controls another Pirate")
    void hasMenaceWithAnotherPirate() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player1, createPirate());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not have menace when alone (no other Pirate)")
    void noMenaceWithoutAnotherPirate() {
        harness.addToBattlefield(player1, new HeadstrongBrute());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Does not have menace with a non-Pirate creature")
    void noMenaceWithNonPirate() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Loses menace when the other Pirate leaves the battlefield")
    void losesMenaceWhenPirateLeaves() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player1, createPirate());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isTrue();

        // Remove the other Pirate
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Pirate does not grant menace")
    void opponentPirateDoesNotGrantMenace() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player2, createPirate());

        Permanent brute = findPermanent(player1, "Headstrong Brute");
        assertThat(gqs.hasKeyword(gd, brute, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Two Headstrong Brutes grant each other menace")
    void twoHeadstrongBrutesGrantEachOtherMenace() {
        harness.addToBattlefield(player1, new HeadstrongBrute());
        harness.addToBattlefield(player1, new HeadstrongBrute());

        List<Permanent> brutes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Headstrong Brute"))
                .toList();

        assertThat(brutes).hasSize(2);
        assertThat(gqs.hasKeyword(gd, brutes.get(0), Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, brutes.get(1), Keyword.MENACE)).isTrue();
    }

    // ===== Helper methods =====

    private Card createPirate() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.PIRATE));
        return card;
    }

}
