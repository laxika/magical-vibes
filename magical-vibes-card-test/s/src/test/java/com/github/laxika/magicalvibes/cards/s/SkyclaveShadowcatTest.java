package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
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

@CardUsed({SkyclaveShadowcat.class, GrizzlyBears.class, CanopyBaloth.class})
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

    @Test
    @DisplayName("Skyclave Shadowcat draws for its own death when it has a +1/+1 counter")
    void counteredShadowcatDeathDrawsCard() {
        Permanent shadowcat = addShadowcat();
        shadowcat.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new SkyclaveShadowcat()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        shadowcat.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyclave Shadowcat");
        harness.assertInGraveyard(player1, "Skyclave Shadowcat");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Simultaneous deaths draw for both the countered Shadowcat and its countered ally")
    void simultaneousCounteredDeathsDrawTwoCards() {
        Permanent shadowcat = addShadowcat();
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        shadowcat.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new SkyclaveShadowcat(), new SkyclaveShadowcat()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        shadowcat.setMarkedDamage(4);
        ally.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skyclave Shadowcat");
        harness.assertInGraveyard(player1, "Canopy Baloth");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("An opponent's countered creature dying does not draw a card")
    void counteredOpponentDeathDoesNotDrawCard() {
        addShadowcat();
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new SkyclaveShadowcat()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        dying.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Canopy Baloth");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Sacrificing a creature with several counters draws once before the ability adds its counter")
    void counteredSacrificeDrawsOnceBeforeCounterResolves() {
        Permanent shadowcat = addShadowcat();
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLibrary(player1, List.of(new SkyclaveShadowcat(), new SkyclaveShadowcat()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Canopy Baloth");
        assertThat(shadowcat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(shadowcat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(shadowcat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("A creature dying with lethal -1/-1 counters still triggers using its +1/+1 counters at death")
    void lethalMinusCountersDoNotEraseDeathTrigger() {
        addShadowcat();
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        harness.setLibrary(player1, List.of(new SkyclaveShadowcat()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Canopy Baloth");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
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
