package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({GaeasWill.class, Forest.class, DarkRitual.class, GoblinRaider.class, Shock.class, PithingNeedle.class})
class GaeasWillTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Gaea's Will with four time counters")
    void suspendExilesWithFourTimeCounters() {
        GaeasWill card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    @DisplayName("The suspended spell lets its controller play lands and cast spells from the graveyard")
    void suspendedSpellAllowsGraveyardPlayAndCast() {
        GaeasWill card = suspendCard();
        Forest forest = new Forest();
        DarkRitual ritual = new DarkRitual();
        harness.setGraveyard(player1, List.of(forest, ritual));

        resolveSuspendedCard();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.playGraveyardLand(player1, 0);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card, ritual);
    }

    @Test
    @DisplayName("The graveyard replacement expires at the end of the turn")
    void graveyardReplacementExpiresAtEndOfTurn() {
        suspendCard();
        harness.setHand(player2, List.of());
        resolveSuspendedCard();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock, creature.getCard());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        Permanent nextCreature = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        Shock nextShock = new Shock();
        harness.setHand(player1, List.of(nextShock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, nextCreature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nextShock, nextCreature.getCard());
    }

    @Test
    @DisplayName("Pithing Needle does not prevent the suspend special action")
    void canSuspendWithNameLockedByPithingNeedle() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gaea's Will");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        GaeasWill card = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend removes counters only during its owner's upkeep and casting may be declined")
    void countersOnlyRemovedOnOwnersUpkeepAndCastingMayBeDeclined() {
        GaeasWill card = suspendCard();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard spells still require their normal mana costs")
    void graveyardSpellRequiresNormalManaCost() {
        suspendCard();
        DarkRitual ritual = new DarkRitual();
        harness.setGraveyard(player1, List.of(ritual));
        resolveSuspendedCard();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ritual);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ritual);
    }

    @Test
    @DisplayName("Graveyard creature spells still require sorcery timing")
    void graveyardCreatureRequiresSorceryTiming() {
        suspendCard();
        GoblinRaider creature = new GoblinRaider();
        harness.setGraveyard(player1, List.of(creature));
        resolveSuspendedCard();
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Goblin Raider");
    }

    @Test
    @DisplayName("Graveyard land permission does not grant an additional land play")
    void cannotPlaySecondLandFromGraveyard() {
        suspendCard();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setGraveyard(player1, List.of(first, second));
        resolveSuspendedCard();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    @DisplayName("Graveyard spell and land permissions expire at end of turn")
    void graveyardPlayPermissionsExpireAtEndOfTurn() {
        suspendCard();
        Forest forest = new Forest();
        DarkRitual ritual = new DarkRitual();
        harness.setGraveyard(player1, List.of(forest, ritual));
        harness.setHand(player2, List.of());
        resolveSuspendedCard();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, ritual.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest, ritual);
    }

    @Test
    @DisplayName("An opponent's resolved spell still goes to their graveyard")
    void opponentsCardsAreNotExiled() {
        harness.setLife(player1, 20);
        suspendCard();
        resolveSuspendedCard();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(shock);
        harness.assertLife(player1, 18);
    }

    private GaeasWill suspendCard() {
        GaeasWill card = new GaeasWill();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void resolveSuspendedCard() {
        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }
}
