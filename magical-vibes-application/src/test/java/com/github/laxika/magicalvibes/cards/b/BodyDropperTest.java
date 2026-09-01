package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BodyDropper.class, GrizzlyBears.class})
class BodyDropperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Body Dropper")
    void sacrificingAnotherCreaturePutsCounterOnSource() {
        Permanent bodyDropper = addReadyBodyDropper();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bodyDropper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    @DisplayName("The activated ability grants menace until end of turn")
    void activatedAbilityGrantsMenace() {
        Permanent bodyDropper = addReadyBodyDropper();
        harness.addToBattlefield(player1, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bodyDropper, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Menace wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent bodyDropper = addReadyBodyDropper();
        harness.addToBattlefield(player1, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bodyDropper, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bodyDropper, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Body Dropper itself")
    void cannotSacrificeItself() {
        addReadyBodyDropper();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBodyDropper() {
        Permanent bodyDropper = harness.addToBattlefieldAndReturn(player1, new BodyDropper());
        bodyDropper.setSummoningSick(false);
        return bodyDropper;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
