package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({LoseCalm.class, GrizzlyBears.class, Pacifism.class})
class LoseCalmTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of the target creature, untaps it, and grants haste and menace")
    void resolvesAllEffects() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castLoseCalm(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Control, haste, and menace expire at cleanup")
    void effectsExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castLoseCalm(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);

        harness.setHand(player1, List.of(new LoseCalm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    private void castLoseCalm(Permanent target) {
        harness.setHand(player1, List.of(new LoseCalm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());
    }
}
