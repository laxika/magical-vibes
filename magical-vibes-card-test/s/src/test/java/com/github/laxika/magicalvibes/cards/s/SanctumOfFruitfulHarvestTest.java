package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctumOfFruitfulHarvest.class, SanctumOfTranquilLight.class})
class SanctumOfFruitfulHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("At your first main phase, adds one mana of one color for each Shrine you control")
    void addsManaForEachShrineYouControl() {
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's first main phase")
    void doesNotTriggerOnOpponentsFirstMainPhase() {
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("All mana from one resolution is added in the chosen color")
    void addsEntireAmountInChosenColor(ManaColor color) {
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Counts itself but not an opponent's Shrines")
    void countsOnlyControlledShrines() {
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());
        harness.addToBattlefield(player2, new SanctumOfTranquilLight());

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Counts Shrines when the ability resolves, including newly arrived Shrines")
    void countsShrinesAtResolution() {
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());

        advanceToPrecombatMain(player1);
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability survives its source leaving but counts only remaining Shrines")
    void resolvesAfterSourceLeavesBattlefield() {
        var source = harness.addToBattlefieldAndReturn(player1, new SanctumOfFruitfulHarvest());
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());

        advanceToPrecombatMain(player1);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds no mana when no Shrines remain at resolution")
    void addsNoManaWithoutRemainingShrines() {
        var source = harness.addToBattlefieldAndReturn(player1, new SanctumOfFruitfulHarvest());

        advanceToPrecombatMain(player1);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at the beginning of the second main phase")
    void doesNotTriggerInPostcombatMainPhase() {
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
