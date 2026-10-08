package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterWeird.class, Forest.class})
class WaterWeirdTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage with a nonland top card puts a +1/+1 counter on Water Weird")
    void nonlandTopCardPutsCounterOnWaterWeird() {
        Permanent weird = addAttackingWaterWeird();
        harness.setLibrary(player1, List.of(new WaterWeird(), new Forest()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(weird.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage with a land top card offers to mill it")
    void landTopCardMayBeMilled() {
        Permanent weird = addAttackingWaterWeird();
        harness.setLibrary(player1, List.of(new Forest(), new WaterWeird()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(weird.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The combat-damage mill is optional")
    void mayDeclineToMill() {
        addAttackingWaterWeird();
        harness.setLibrary(player1, List.of(new Forest(), new WaterWeird()));

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The pump ability gives +1/-1 until end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent weird = addCreatureReady(player1, new WaterWeird());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isZero();
        assertThat(weird.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An empty library still allows choosing the otherwise mill branch")
    void emptyLibraryAllowsOptionalMill() {
        Permanent weird = addAttackingWaterWeird();
        harness.setLibrary(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(weird.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Water Weird");
    }

    @Test
    @DisplayName("The top card is checked when the combat damage trigger resolves")
    void checksLibraryAtResolution() {
        Permanent weird = addAttackingWaterWeird();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.setLibrary(player1, List.of(new WaterWeird()));
        resolveAllTriggers();

        assertThat(weird.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The pump can be activated while summoning sick and repeated until toughness is zero")
    void repeatedPumpCanKillSummoningSickWaterWeird() {
        harness.addToBattlefield(player1, new WaterWeird());
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int activation = 0; activation < 4; activation++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Water Weird");
        harness.assertInGraveyard(player1, "Water Weird");
    }

    private Permanent addAttackingWaterWeird() {
        Permanent weird = addCreatureReady(player1, new WaterWeird());
        weird.setAttacking(true);
        return weird;
    }
}
