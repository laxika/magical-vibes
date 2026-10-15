package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoliathMassManipulator.class, AirElemental.class})
class GoliathMassManipulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Power-up puts two counters on Goliath and draws for creatures with power four or greater")
    void powerUpAddsCountersAndDrawsForQualifyingCreatures() {
        Card firstDraw = new AirElemental();
        Card secondDraw = new AirElemental();
        var goliath = harness.enterBattlefieldAndReturn(player1, new GoliathMassManipulator());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goliath.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Power-up costs its full activation cost after the entry turn")
    void powerUpIsNotDiscountedAfterEntryTurn() {
        var goliath = addCreatureReady(player1, new GoliathMassManipulator());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goliath.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new GoliathMassManipulator());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Entry-turn power-up can be paid with exactly three generic mana")
    void entryTurnDiscountRemovesBothGenericAndGreenMana() {
        Card draw = new GoliathMassManipulator();
        var goliath = harness.enterBattlefieldAndReturn(player1, new GoliathMassManipulator());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goliath.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("Draw counts current power and excludes opponents' creatures")
    void drawExcludesCreaturesBelowFourPowerAndOpponentsCreatures() {
        Card draw = new GoliathMassManipulator();
        addCreatureReady(player1, new GoliathMassManipulator());
        var smallCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        smallCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw, new GoliathMassManipulator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Power-up draws nothing when no creature reaches four power")
    void noQualifyingCreaturesDrawsNoCards() {
        var goliath = addCreatureReady(player1, new GoliathMassManipulator());
        goliath.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GoliathMassManipulator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, goliath)).isEqualTo(3);
    }

    @Test
    @DisplayName("Draw still resolves if Goliath leaves before his power-up resolves")
    void sourceLeavingDoesNotPreventDrawingForRemainingCreatures() {
        Card draw = new GoliathMassManipulator();
        var goliath = addCreatureReady(player1, new GoliathMassManipulator());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw, new GoliathMassManipulator()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, goliath));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(goliath.getCard());
    }

    @Test
    @DisplayName("A discounted payment is insufficient after the entry turn")
    void oldPermanentCannotUseEntryTurnDiscount() {
        addCreatureReady(player1, new GoliathMassManipulator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
