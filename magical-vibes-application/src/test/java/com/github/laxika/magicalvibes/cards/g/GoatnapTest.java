package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoatnapTest extends BaseCardTest {

    private static Card goat() {
        Card card = new Card();
        card.setName("Mtenda Goat");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.WHITE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.GOAT));
        return card;
    }

    private void castGoatnap(Permanent target) {
        harness.setHand(player1, List.of(new Goatnap()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Goatnap untaps, steals, grants haste, and boosts a Goat")
    void resolvesAgainstGoat() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, goat());
        target.tap();

        castGoatnap(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goatnap does not boost a non-Goat creature")
    void doesNotBoostNonGoat() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castGoatnap(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Goatnap's temporary effects expire at cleanup")
    void temporaryEffectsExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, goat());

        castGoatnap(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Goatnap only targets creatures")
    void rejectsNonCreaturePermanent() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent nonCreature = new Permanent(new com.github.laxika.magicalvibes.cards.p.Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);
        harness.setHand(player1, List.of(new Goatnap()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
