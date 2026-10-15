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
    @DisplayName("An archmage with a -1/-1 counter can counter a spell but does not persist again")
    void counterOnArchmagePreventsPersistButNotActivation() {
        Permanent archmage = addCreatureReady(player1, new GlenElendraArchmage());
        archmage.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        archmage.tap();
        archmage.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Glen Elendra Archmage");
        harness.assertInGraveyard(player1, "Glen Elendra Archmage");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Persist also returns the archmage after lethal damage")
    void persistReturnsAfterLethalDamage() {
        Permanent archmage = addCreatureReady(player1, new GlenElendraArchmage());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, archmage.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Glen Elendra Archmage");
        assertThat(returned.getId()).isNotEqualTo(archmage.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Glen Elendra Archmage");
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
    @DisplayName("The counter ability requires blue mana and does not sacrifice on a rejected activation")
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new GlenElendraArchmage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Glen Elendra Archmage");
        harness.assertNotInGraveyard(player1, "Glen Elendra Archmage");
        resolveAllTriggers();
        harness.assertLife(player1, 18);
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
