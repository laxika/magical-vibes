package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

class FlowstoneSculptureTest extends BaseCardTest {

    private static final String COUNTER_MODE = "Put a +1/+1 counter on this creature.";
    private static final String FLYING_MODE = "This creature gains flying.";
    private static final String FIRST_STRIKE_MODE = "This creature gains first strike.";
    private static final String TRAMPLE_MODE = "This creature gains trample.";

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on it and the discard cost is paid")
    void counterMode() {
        Permanent sculpture = addSculpture();

        activate();
        harness.handleListChoice(player1, COUNTER_MODE);

        assertThat(sculpture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sculpture)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sculpture)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Flying mode grants flying and it lasts past end of turn")
    void flyingModeLastsIndefinitely() {
        Permanent sculpture = addSculpture();

        activate();
        harness.handleListChoice(player1, FLYING_MODE);

        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("First strike mode grants first strike")
    void firstStrikeMode() {
        Permanent sculpture = addSculpture();

        activate();
        harness.handleListChoice(player1, FIRST_STRIKE_MODE);

        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample mode grants trample")
    void trampleMode() {
        Permanent sculpture = addSculpture();

        activate();
        harness.handleListChoice(player1, TRAMPLE_MODE);

        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sculpture, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An unknown mode label is rejected")
    void illegalModeRejected() {
        addSculpture();

        activate();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "This creature gains haste."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cannot be activated with an empty hand")
    void requiresACardToDiscard() {
        addSculpture();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSculpture() {
        Permanent permanent = new Permanent(new FlowstoneSculpture());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    /** Pays {2} and the discard cost, then resolves the ability up to the mode prompt. */
    private void activate() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
