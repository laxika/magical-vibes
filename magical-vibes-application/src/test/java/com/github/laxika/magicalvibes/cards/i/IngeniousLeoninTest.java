package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IngeniousLeonin.class, GrizzlyBears.class})
class IngeniousLeoninTest extends BaseCardTest {

    @Test
    @DisplayName("Ability puts a +1/+1 counter on another attacking Cat and grants first strike")
    void boostsAttackingCat() {
        addSource();
        Permanent cat = addAttackingCreature(player1, new IngeniousLeonin());
        addMana();

        activate(cat);

        assertThat(cat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Ability puts a counter on a non-Cat without granting first strike")
    void boostsNonCatWithoutFirstStrike() {
        addSource();
        Permanent bears = addAttackingCreature(player1, new GrizzlyBears());
        addMana();

        activate(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn while the counter remains")
    void firstStrikeWearsOffAtEndOfTurn() {
        addSource();
        Permanent cat = addAttackingCreature(player1, new IngeniousLeonin());
        addMana();

        activate(cat);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cat, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(cat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot target the source, a nonattacking creature, or an opponent's creature")
    void targetMustBeAnotherAttackingCreatureYouControl() {
        Permanent source = addSource();
        source.setAttacking(true);
        Permanent nonattacking = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addAttackingCreature(player2, new IngeniousLeonin());
        addMana();

        assertThatThrownBy(() -> activate(source)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> activate(nonattacking)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> activate(opponent)).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSource() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        return addCreatureReady(player1, new IngeniousLeonin());
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = addCreatureReady(player, card);
        creature.setAttacking(true);
        return creature;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 4);
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
