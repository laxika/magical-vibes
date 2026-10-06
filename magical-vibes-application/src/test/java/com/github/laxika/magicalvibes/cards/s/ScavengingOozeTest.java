package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengingOoze.class, ElvishMystic.class, Cancel.class})
class ScavengingOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card grows the Ooze and gains 1 life")
    void exilingCreatureCardGrowsOozeAndGainsLife() {
        Permanent ooze = addOoze();
        Card bears = new ElvishMystic();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        activate(ooze, bears);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Elvish Mystic");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elvish Mystic"));
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Exiling a noncreature card gives no counter and no life")
    void exilingNoncreatureCardGivesNoBonus() {
        Permanent ooze = addOoze();
        Card cancel = new Cancel();
        harness.setGraveyard(player1, List.of(cancel));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        activate(ooze, cancel);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Cancel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Can exile a card from an opponent's graveyard")
    void exilesFromOpponentGraveyard() {
        Permanent ooze = addOoze();
        Card bears = new ElvishMystic();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);

        activate(ooze, bears);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Elvish Mystic");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Elvish Mystic"));
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations stack +1/+1 counters")
    void repeatedActivationsStackCounters() {
        Permanent ooze = addOoze();
        Card bears = new ElvishMystic();
        Card otherBears = new ElvishMystic();
        harness.setGraveyard(player1, List.of(bears, otherBears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        activate(ooze, bears);
        harness.passBothPriorities();
        activate(ooze, otherBears);
        harness.passBothPriorities();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent ooze = addOoze();
        Card bears = new ElvishMystic();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        activate(ooze, bears);
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutMana() {
        Permanent ooze = addOoze();
        Card bears = new ElvishMystic();
        harness.setGraveyard(player1, List.of(bears));

        assertThatThrownBy(() -> activate(ooze, bears))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new ScavengingOoze());
        ooze.setSummoningSick(true);
        ooze.setTapped(true);
        Card creature = new ElvishMystic();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        activate(ooze, creature);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Ability still exiles and gains life after the Ooze leaves")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent ooze = addOoze();
        Card creature = new ElvishMystic();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        activate(ooze, creature);
        gd.playerBattlefields.get(player1.getId()).remove(ooze);
        harness.setGraveyard(player1, List.of(ooze.getCard()));
        Permanent otherOoze = addOoze();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        harness.assertNotInGraveyard(player2, "Elvish Mystic");
        assertThat(otherOoze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, lifeBefore + 1);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("Two activations targeting the same card grant only one bonus")
    void competingActivationsGrantOnlyOneBonus() {
        Permanent ooze = addOoze();
        Card creature = new ElvishMystic();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        activate(ooze, creature);
        activate(ooze, creature);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsOnly(creature);
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore + 1);
    }

    private void activate(Permanent ooze, Card target) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ooze);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
    }

    private Permanent addOoze() {
        return addCreatureReady(player1, new ScavengingOoze());
    }
}
