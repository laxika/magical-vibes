package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FearsomeGoblinPair;
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

@CardUsed({IronHills.class, IronHillsBlacksmith.class, FearsomeGoblinPair.class})
class IronHillsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new IronHills()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Iron Hills").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tapsForRedMana() {
        tapFor(ManaColor.RED);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isOne();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tapsForWhiteMana() {
        tapFor(ManaColor.WHITE);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isOne();
    }

    @Test
    @DisplayName("Sacrificing the land puts two +1/+1 counters on a Dwarf you control")
    void sacrificeAbilityPutsCountersOnDwarf() {
        Permanent hills = addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        addCounterAbilityMana();

        harness.activateAbility(player1, 0, 1, null, dwarf.getId());
        harness.passBothPriorities();

        assertThat(dwarf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hills);
        harness.assertInGraveyard(player1, "Iron Hills");
    }

    @Test
    @DisplayName("The counter ability cannot target a non-Dwarf")
    void counterAbilityCannotTargetNonDwarf() {
        addReadyHills();
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new FearsomeGoblinPair());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Dwarf you control");
    }

    @Test
    @DisplayName("The counter ability can only be activated as a sorcery")
    void counterAbilityIsSorcerySpeed() {
        addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        addCounterAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Mana and sacrifice costs are paid before the counter ability resolves")
    void counterAbilityPaysCostsBeforeResolution() {
        addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        addCounterAbilityMana();

        harness.activateAbility(player1, 0, 1, null, dwarf.getId());

        harness.assertNotOnBattlefield(player1, "Iron Hills");
        harness.assertInGraveyard(player1, "Iron Hills");
        assertThat(dwarf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        harness.passBothPriorities();
        assertThat(dwarf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The counter ability cannot target an opponent's Dwarf")
    void counterAbilityCannotTargetOpponentsDwarf() {
        Permanent hills = addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player2, new IronHillsBlacksmith());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Dwarf you control");

        assertThat(hills.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Iron Hills");
        harness.assertNotInGraveyard(player1, "Iron Hills");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isOne();
    }

    @Test
    @DisplayName("A tapped Iron Hills cannot pay the counter ability's tap cost")
    void tappedLandCannotActivateCounterAbility() {
        Permanent hills = addReadyHills();
        hills.tap();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        addCounterAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Iron Hills");
        harness.assertNotInGraveyard(player1, "Iron Hills");
        assertThat(dwarf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter ability requires white mana as well as red and generic mana")
    void counterAbilityCannotActivateWithoutWhiteMana() {
        Permanent hills = addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hills.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Iron Hills");
        harness.assertNotInGraveyard(player1, "Iron Hills");
    }

    @Test
    @DisplayName("The counter ability cannot be activated during an opponent's main phase")
    void counterAbilityCannotActivateOnOpponentsTurn() {
        addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        addCounterAbilityMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The counter ability cannot be activated with another ability on the stack")
    void counterAbilityRequiresEmptyStack() {
        addReadyHills();
        addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        addCounterAbilityMana();
        addCounterAbilityMana();
        harness.activateAbility(player1, 0, 1, null, dwarf.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(dwarf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Iron Hills");
    }

    @Test
    @DisplayName("The counter ability does not resolve if the Dwarf changes controllers")
    void counterAbilityDoesNotPutCountersOnDwarfNoLongerControlled() {
        addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        addCounterAbilityMana();
        harness.activateAbility(player1, 0, 1, null, dwarf.getId());

        gd.playerBattlefields.get(player1.getId()).remove(dwarf);
        gd.playerBattlefields.get(player2.getId()).add(dwarf);
        gd.stolenCreatures.put(dwarf.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(dwarf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Iron Hills");
        harness.assertOnBattlefield(player2, "Iron Hills Blacksmith");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the target does not refund the sacrificed land or mana")
    void counterAbilityLosesTargetBeforeResolution() {
        addReadyHills();
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new IronHillsBlacksmith());
        addCounterAbilityMana();
        harness.activateAbility(player1, 0, 1, null, dwarf.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, dwarf));
        harness.passBothPriorities();

        assertThat(dwarf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Iron Hills");
        harness.assertInGraveyard(player1, "Iron Hills Blacksmith");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void tapFor(ManaColor color) {
        Permanent hills = addReadyHills();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(hills.isTapped()).isTrue();
    }

    private Permanent addReadyHills() {
        Permanent hills = harness.addToBattlefieldAndReturn(player1, new IronHills());
        hills.untap();
        return hills;
    }

    private void addCounterAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
