package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SkyclaveShadowcat.class, GrizzlyBears.class})
class SkyclaveShadowcatTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Skyclave Shadowcat")
    void sacrificeAnotherCreaturePutsCounterOnShadowcat() {
        Permanent shadowcat = addShadowcat();
        harness.addToBattlefield(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shadowcat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The death trigger draws for a controlled creature with a +1/+1 counter")
    void counteredAllyDeathDrawsCard() {
        addShadowcat();
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        setSingleCardLibrary();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        dying.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The death trigger ignores a controlled creature without a +1/+1 counter")
    void counterlessAllyDeathDoesNotDrawCard() {
        addShadowcat();
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setSingleCardLibrary();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        dying.setMarkedDamage(3);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Skyclave Shadowcat itself")
    void cannotSacrificeShadowcatItself() {
        addShadowcat();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addShadowcat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new SkyclaveShadowcat());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void setSingleCardLibrary() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
    }
}
