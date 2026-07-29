package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JabarisInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Steals the attacker and shrinks its power by one")
    void stealsAttackerAndPutsMinusOneMinusZeroCounter() {
        Permanent bears = attackPlayer2With(new GrizzlyBears());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new JabarisInfluence()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        // 2/2 base, -1/-0 counter.
        assertThat(harness.getGameQueryService().getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that did not attack this turn")
    void cannotTargetNonAttacker() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new JabarisInfluence()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black attacker")
    void cannotTargetBlackAttacker() {
        Permanent imp = attackPlayer2With(blackCreature());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new JabarisInfluence()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, imp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast before combat has ended")
    void cannotCastBeforeCombatEnds() {
        Permanent bears = attackPlayer2With(new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new JabarisInfluence()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    /** Declares {@code card} (controlled by player1) as an attacker against player2 through the engine. */
    private Permanent attackPlayer2With(Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        battlefield.add(perm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(battlefield.indexOf(perm)));
        return perm;
    }

    private Card blackCreature() {
        Card card = new Card();
        card.setName("Bog Imp");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{B}");
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
