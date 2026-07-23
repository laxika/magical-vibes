package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdarkarUnicornTest extends BaseCardTest {

    private Permanent unicornOnBattlefield() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new AdarkarUnicorn());
        unicorn.setSummoningSick(false);
        return unicorn;
    }

    private void activateAddU() {
        harness.activateAbility(player1, 0, 0, null, null);
    }

    private void activateAddCU() {
        harness.activateAbility(player1, 0, 1, null, null);
    }

    private Permanent permanentWithCumulativeUpkeepU(Player controller) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        TestCards.mutableCard(permanent).addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{U}"));
        return permanent;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("First mode adds one cumulative-upkeep-only blue")
    void firstModeAddsBlue() {
        unicornOnBattlefield();

        activateAddU();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("Second mode adds cumulative-upkeep-only {C}{U}")
    void secondModeAddsColorlessAndBlue() {
        unicornOnBattlefield();

        activateAddCU();

        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColorless()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cumulative-upkeep-only mana cannot pay a normal spell")
    void cannotPayNormalSpell() {
        unicornOnBattlefield();
        activateAddU();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cumulative-upkeep-only mana pays cumulative upkeep and keeps the permanent")
    void paysCumulativeUpkeep() {
        Permanent unicorn = unicornOnBattlefield();
        Permanent bears = permanentWithCumulativeUpkeepU(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve CU trigger → may-pay

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(bears.getCounterCount(CounterType.AGE)).isEqualTo(1);

        // Mana empties between steps — produce CU mana at payment time (as in real play).
        activateAddU();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE)).isZero();
        assertThat(unicorn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices the permanent")
    void declineSacrifices() {
        unicornOnBattlefield();
        Permanent bears = permanentWithCumulativeUpkeepU(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
