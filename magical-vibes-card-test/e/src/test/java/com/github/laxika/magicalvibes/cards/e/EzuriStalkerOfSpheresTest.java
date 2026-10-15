package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EzuriStalkerOfSpheres.class, CopperLonglegs.class})
class EzuriStalkerOfSpheresTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3} proliferates twice and draws twice")
    void payingManaProliferatesTwiceAndDrawsTwice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new EzuriStalkerOfSpheres()));
        harness.setLibrary(player1, List.of(new CopperLonglegs(), new CopperLonglegs()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        resolveAllTriggers();
        harness.assertInHand(player1, "Copper Longlegs");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the entry payment skips both proliferate and draw")
    void decliningManaSkipsProliferateAndDraw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new EzuriStalkerOfSpheres()));
        harness.setLibrary(player1, List.of(new CopperLonglegs()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInHand(player1, "Copper Longlegs");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsTwiceWhenNoPermanentsOrPlayersHaveCounters() {
        castEzuriAndPay();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsTwiceWhenChoosingNothingBothTimes() {
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castEzuriAndPay();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void secondProliferationCanChoosePlayersWithEnergyAndExperienceCounters() {
        gd.playerEnergyCounters.put(player1.getId(), 1);
        gd.playerExperienceCounters.put(player2.getId(), 1);
        castEzuriAndPay();

        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void drawsForAnotherSourceControlledByYouProliferating() {
        harness.addToBattlefield(player1, new CopperLonglegs());
        harness.addToBattlefield(player1, new EzuriStalkerOfSpheres());
        harness.setLibrary(player1, List.of(new CopperLonglegs()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertInHand(player1, "Copper Longlegs");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenOpponentProliferates() {
        harness.addToBattlefield(player1, new EzuriStalkerOfSpheres());
        harness.addToBattlefield(player2, new CopperLonglegs());
        harness.setLibrary(player1, List.of(new CopperLonglegs()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Copper Longlegs");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void castEzuriAndPay() {
        harness.setHand(player1, List.of(new EzuriStalkerOfSpheres()));
        harness.setLibrary(player1, List.of(new CopperLonglegs(), new CopperLonglegs()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
    }

}
