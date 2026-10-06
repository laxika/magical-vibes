package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousAmulet.class, GrizzlyBears.class, LlanowarElves.class})
class RavenousAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature draws a card and adds a soul counter")
    void sacrificingCreatureDrawsAndAddsSoulCounter() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new RavenousAmulet());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof LlanowarElves);
        assertThat(amulet.getCounterCount(CounterType.SOUL)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Sacrificing the amulet makes each opponent lose life equal to its soul counters")
    void sacrificingAmuletMakesEachOpponentLoseSoulCounters() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new RavenousAmulet());
        amulet.setCounterCount(CounterType.SOUL, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amulet);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(amulet.getCard());
    }

    @Test
    @DisplayName("The draw ability requires a creature to sacrifice")
    void drawAbilityRequiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new RavenousAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureSacrificeAndTapArePaidBeforeDrawing() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new RavenousAmulet());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(amulet.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(amulet.getCounterCount(CounterType.SOUL)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(amulet.getCounterCount(CounterType.SOUL)).isEqualTo(1);
    }

    @Test
    void drawAbilityCannotBeActivatedDuringUpkeep() {
        harness.addToBattlefield(player1, new RavenousAmulet());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void drawAbilityCannotBeActivatedOnOpponentsTurn() {
        harness.addToBattlefield(player1, new RavenousAmulet());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void drawAbilityRequiresEmptyStack() {
        harness.addToBattlefield(player1, new RavenousAmulet());
        harness.addToBattlefield(player1, new RavenousAmulet());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 1, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
    }

    @Test
    void opponentsCreatureCannotPaySacrificeCost() {
        harness.addToBattlefield(player1, new RavenousAmulet());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void amuletWithNoSoulCountersCausesNoLifeLoss() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new RavenousAmulet());
        amulet.setCounterCount(CounterType.CHARGE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(amulet.getCard());
    }

    @Test
    void lifeLossAbilityCanBeActivatedOnOpponentsUpkeep() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new RavenousAmulet());
        amulet.setCounterCount(CounterType.SOUL, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void tappedAmuletCannotActivateEitherAbility() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new RavenousAmulet());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        amulet.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(amulet);
    }
}
