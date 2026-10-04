package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArcumsAstrolabe;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
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

@CardUsed({Goatnap.class, UniversalAutomaton.class, MotherBear.class, ArcumsAstrolabe.class})
class GoatnapTest extends BaseCardTest {

    private void castGoatnap(Permanent target) {
        harness.setHand(player1, List.of(new Goatnap()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Goatnap untaps, steals, grants haste, and boosts a Goat")
    void resolvesAgainstGoat() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());
        target.tap();

        castGoatnap(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Goatnap does not boost a non-Goat creature")
    void doesNotBoostNonGoat() {
        Permanent target = addCreatureReady(player2, new MotherBear());

        castGoatnap(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Goatnap's temporary effects expire at cleanup")
    void temporaryEffectsExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());

        castGoatnap(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Goatnap only targets creatures")
    void rejectsNonCreaturePermanent() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new ArcumsAstrolabe());
        harness.setHand(player1, List.of(new Goatnap()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Goatnap can untap and boost a creature already controlled by its caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton());
        target.tap();

        castGoatnap(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Goatnap does not affect a creature that left and returned before resolution")
    void doesNotAffectReturnedCreature() {
        UniversalAutomaton creature = new UniversalAutomaton();
        Permanent original = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setHand(player1, List.of(new Goatnap()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, original.getId());

        gd.playerBattlefields.get(player2.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, creature);
        returned.tap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(returned);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Goatnap");
    }
}
