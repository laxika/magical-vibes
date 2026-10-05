package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.d.Dispel;
import com.github.laxika.magicalvibes.cards.h.HalimarDepths;
import com.github.laxika.magicalvibes.cards.k.KhalniGarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaceTheMindSculptor.class, Dispel.class, KhalniGarden.class, HalimarDepths.class, ArborElf.class})
class JaceTheMindSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("+2 looks at a target player's top card and can put it on the bottom")
    void plusTwoCanPutTargetPlayersTopCardOnBottom() {
        Permanent jace = addReadyJace(player1, 3);
        Card top = new Dispel();
        Card next = new KhalniGarden();
        harness.setLibrary(player2, List.of(top, next));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next, top);
    }

    @Test
    @DisplayName("0 draws three cards and puts two chosen cards on top in order")
    void zeroDrawsThreeAndPutsTwoOnTop() {
        Permanent jace = addReadyJace(player1, 3);
        Card first = new Dispel();
        Card second = new KhalniGarden();
        Card third = new HalimarDepths();
        Card fourth = new ArborElf();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, fourth);
    }

    @Test
    @DisplayName("-1 returns a target creature to its owner's hand")
    void minusOneReturnsTargetCreature() {
        Permanent jace = addReadyJace(player1, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArborElf());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Arbor Elf");
        harness.assertInHand(player2, "Arbor Elf");
    }

    @Test
    @DisplayName("-1 cannot target a land")
    void minusOneCannotTargetLand() {
        addReadyJace(player1, 3);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new HalimarDepths());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-12 exiles the target library, then shuffles that player's hand into it")
    void minusTwelveExilesLibraryAndShufflesHandIntoIt() {
        Permanent jace = addReadyJace(player1, 12);
        Card libraryCard = new Dispel();
        Card secondLibraryCard = new KhalniGarden();
        Card handCard = new HalimarDepths();
        harness.setLibrary(player2, List.of(libraryCard, secondLibraryCard));
        harness.setHand(player2, List.of(handCard));

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(libraryCard, secondLibraryCard);
        harness.assertNotOnBattlefield(player1, "Jace, the Mind Sculptor");
    }

    @Test
    void plusTwoMovesTheLookedAtCardDuringTheSameResolution() {
        addReadyJace(player1, 3);
        Card top = new Dispel();
        Card next = new ArborElf();
        harness.setLibrary(player2, List.of(top, next));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next, top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusTwoCanKeepOwnTopCard() {
        addReadyJace(player1, 3);
        Card top = new Dispel();
        Card next = new ArborElf();
        harness.setLibrary(player1, List.of(top, next));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player2, true))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
    }

    @Test
    void plusTwoWithEmptyLibraryNeedsNoChoice() {
        Permanent jace = addReadyJace(player1, 3);
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void zeroCanReturnPreviouslyHeldCardsInReverseOrder() {
        addReadyJace(player1, 3);
        Card heldFirst = new Dispel();
        Card heldSecond = new ArborElf();
        Card drawnFirst = new KhalniGarden();
        Card drawnSecond = new HalimarDepths();
        Card drawnThird = new Dispel();
        Card remaining = new ArborElf();
        harness.setHand(player1, List.of(heldFirst, heldSecond));
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond, drawnThird, remaining));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(heldFirst.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(heldSecond.getId(), heldFirst.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond, drawnThird);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(heldSecond, heldFirst, remaining);
    }

    @Test
    void minusOneReturnsStolenCreatureToOwner() {
        addReadyJace(player1, 3);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArborElf());
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arbor Elf");
        harness.assertInHand(player2, "Arbor Elf");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void minusTwelveCanTargetSelfWithEmptyLibrary() {
        addReadyJace(player1, 13);
        Card first = new Dispel();
        Card second = new ArborElf();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(first, second));

        harness.activateAbility(player1, 0, 3, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Jace, the Mind Sculptor");
    }

    @Test
    void minusTwelveCannotBeActivatedWithInsufficientLoyalty() {
        Permanent jace = addReadyJace(player1, 11);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough loyalty");

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(11);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyJace(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JaceTheMindSculptor());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
