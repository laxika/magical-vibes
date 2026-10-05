package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ScorchingShot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaceReawakened.class, ColossalDreadmaw.class, GrizzlyBears.class, Mountain.class, ScorchingShot.class})
class JaceReawakenedTest extends BaseCardTest {

    @Test
    void cannotBeCastDuringFirstThreeTurns() {
        harness.setHand(player1, List.of(new JaceReawakened()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jace Reawakened");
    }

    @Test
    void firstAbilityDrawsThenDiscards() {
        Mountain drawnCard = new Mountain();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void secondAbilityPlotsEligibleCard() {
        GrizzlyBears eligible = new GrizzlyBears();
        Mountain land = new Mountain();
        ColossalDreadmaw tooExpensive = new ColossalDreadmaw();
        harness.setHand(player1, List.of(eligible, land, tooExpensive));
        addReadyJace(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(eligible);
        assertThat(gd.plottedCardIds).containsExactly(eligible.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, tooExpensive);
    }

    @Test
    void ultimateCopiesCreatureSpellsAfterJaceLeaves() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Jace Reawakened");

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears")))
                .hasSize(2);
    }

    @Test
    void castingRestrictionCountsOwnTurnsRatherThanGameTurns() {
        harness.setHand(player1, List.of(new JaceReawakened()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        gd.turnNumber = 20;

        for (int ownTurn = 1; ownTurn <= 3; ownTurn++) {
            gd.turnsTakenByPlayer.put(player1.getId(), ownTurn);
            assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
        harness.assertInHand(player1, "Jace Reawakened");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void firstAbilityCanDiscardTheCardJustDrawn() {
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of());
        addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    void secondAbilityMayBeDeclined() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setHand(player1, List.of(eligible));
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondAbilityWithNoEligibleCardsDoesNotExileAnything() {
        Mountain land = new Mountain();
        ColossalDreadmaw tooExpensive = new ColossalDreadmaw();
        harness.setHand(player1, List.of(land, tooExpensive));
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, tooExpensive);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void plottedCardCanBeCastForFreeOnlyOnALaterTurnAtSorcerySpeed() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setHand(player1, List.of(eligible));
        addReadyJace(player1);
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, eligible.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("turn it became plotted");

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, eligible.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, eligible.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(eligible);
    }

    @Test
    void ultimateAllowsNewTargetsForTheCopyWithoutChangingTheOriginal() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 6);
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ScorchingShot()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, originalTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(originalTarget.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ultimateExpiresAtEndOfTurn() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 6);
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void ultimateCopyKeepsOriginalTargetWhenRetargetingIsDeclined() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ScorchingShot()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player1, "Scorching Shot");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ultimateCopyOfCreatureSpellResolvesAsATokenWithoutTriggeringAnotherCopy() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 6);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().isToken()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    private Permanent addReadyJace(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JaceReawakened());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
