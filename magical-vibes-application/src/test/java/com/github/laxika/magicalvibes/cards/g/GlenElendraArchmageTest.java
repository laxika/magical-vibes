package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({GlenElendraArchmage.class, Shock.class, LlanowarElves.class})
class GlenElendraArchmageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability counters a noncreature spell")
    void sacrificeCountersNoncreatureSpell() {
        addCreatureReady(player1, new GlenElendraArchmage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20); // Shock was countered, no damage
    }

    @Test
    @DisplayName("Persist returns the archmage with a -1/-1 counter after it is sacrificed")
    void persistReturnsArchmageAfterSacrifice() {
        addCreatureReady(player1, new GlenElendraArchmage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Glen Elendra Archmage");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        addCreatureReady(player1, new GlenElendraArchmage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        Permanent ownArchmage = addCreatureReady(player1, new GlenElendraArchmage());
        Permanent opposingArchmage = addCreatureReady(player2, new GlenElendraArchmage());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        int opposingArchmageIndex = gd.playerBattlefields.get(player2.getId()).indexOf(opposingArchmage);
        harness.activateAbility(player2, opposingArchmageIndex, null, shock.getId());
        harness.passPriority(player2);

        int ownArchmageIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ownArchmage);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, ownArchmageIndex, null, opposingArchmage.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
