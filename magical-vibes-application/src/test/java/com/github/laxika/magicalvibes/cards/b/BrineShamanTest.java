package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({BrineShaman.class, BalduvianBears.class, DarkRitual.class})
class BrineShamanTest extends BaseCardTest {

    // ===== {T}, Sacrifice a creature: Target creature gets +2/+2 =====

    @Test
    @DisplayName("Sacrificing a creature gives target creature +2/+2 and taps Brine Shaman")
    void sacrificeCreatureGivesPlusTwo() {
        addCreatureReady(player1, new BrineShaman());
        // Only creature player1 controls is the Shaman → sacrifice cost auto-picks it.
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("The +2/+2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new BrineShaman());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can sacrifice another creature while tapping Brine Shaman")
    void canSacrificeAnotherCreatureWhileTappingShaman() {
        Permanent shaman = addCreatureReady(player1, new BrineShaman());
        Permanent fodder = addCreatureReady(player1, new BalduvianBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shaman).doesNotContain(fodder);
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    // ===== {1}{U}{U}, Sacrifice a creature: Counter target creature spell =====

    @Test
    @DisplayName("Counters target creature spell, paying mana and sacrificing a creature")
    void countersCreatureSpell() {
        addCreatureReady(player1, new BrineShaman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        BalduvianBears spell = new BalduvianBears();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        // Only creature player1 controls is the Shaman → sacrifice cost auto-picks it.
        harness.activateAbility(player1, 0, 1, null, spell.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Balduvian Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can sacrifice another creature to counter a creature spell")
    void canSacrificeAnotherCreatureToCounterCreatureSpell() {
        Permanent shaman = addCreatureReady(player1, new BrineShaman());
        Permanent fodder = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        BalduvianBears spell = new BalduvianBears();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, spell.getId());
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(shaman);
        assertThat(shaman.isTapped()).isFalse();

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        addCreatureReady(player1, new BrineShaman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        DarkRitual darkRitual = new DarkRitual();
        harness.setHand(player2, List.of(darkRitual));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, darkRitual.getId())
        ).isInstanceOf(IllegalStateException.class);
    }
}
