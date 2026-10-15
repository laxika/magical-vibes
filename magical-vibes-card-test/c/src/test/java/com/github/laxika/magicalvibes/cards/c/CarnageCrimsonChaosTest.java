package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarnageCrimsonChaos.class, BenalishKnight.class, GrizzlyBears.class, HillGiant.class, HolyDay.class})
class CarnageCrimsonChaosTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets a creature card with mana value 3 or less")
    void etbTargetsEligibleCreature() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive, nonCreature));

        castCarnageFromHand();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The returned creature must attack each combat if able")
    void returnedCreatureMustAttack() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        castCarnageFromHand();
        chooseReturnedCreature(eligible);

        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        returned.setSummoningSick(false);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The returned creature is sacrificed after dealing combat damage to a player")
    void returnedCreatureIsSacrificedAfterCombatDamage() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        castCarnageFromHand();
        chooseReturnedCreature(eligible);

        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        returned.setSummoningSick(false);
        returned.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mayhem casts Carnage from the graveyard after it was discarded this turn")
    void mayhemCastsFromGraveyard() {
        GrizzlyBears eligible = new GrizzlyBears();
        CarnageCrimsonChaos carnage = new CarnageCrimsonChaos();
        harness.setGraveyard(player1, List.of(eligible, carnage));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(carnage.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromGraveyard(player1, 1);
        harness.passBothPriorities();
        chooseReturnedCreature(eligible);

        harness.assertOnBattlefield(player1, "Carnage, Crimson Chaos");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Carnage enters even when no eligible graveyard target exists")
    void entersWithoutEligibleTarget() {
        harness.setGraveyard(player1, List.of(new HillGiant(), new HolyDay()));

        castCarnageFromHand();

        harness.assertOnBattlefield(player1, "Carnage, Crimson Chaos");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Holy Day");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ETB cannot target a creature in an opponent's graveyard")
    void etbOnlyTargetsOwnGraveyard() {
        GrizzlyBears ownCreature = new GrizzlyBears();
        GrizzlyBears opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        castCarnageFromHand();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        chooseReturnedCreature(ownCreature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A graveyard target that leaves before resolution is not returned")
    void missingTargetIsNotReturned() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        castCarnageFromHand();
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Carnage, Crimson Chaos");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The returned creature is not required to attack while summoning sick")
    void summoningSickCreatureDoesNotHaveToAttack() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        castCarnageFromHand();
        chooseReturnedCreature(eligible);
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevented combat damage does not trigger the sacrifice ability")
    void preventedDamageDoesNotSacrificeReturnedCreature() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        castCarnageFromHand();
        chooseReturnedCreature(eligible);
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        returned.setSummoningSick(false);
        returned.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mayhem cannot cast Carnage if it was not discarded this turn")
    void mayhemRequiresCarnageToHaveBeenDiscarded() {
        harness.setGraveyard(player1, List.of(new CarnageCrimsonChaos()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Carnage, Crimson Chaos");
        harness.assertNotOnBattlefield(player1, "Carnage, Crimson Chaos");
    }

    @Test
    @DisplayName("Discarding another card does not make Carnage's mayhem available")
    void discardingAnotherCardDoesNotEnableMayhem() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new CarnageCrimsonChaos(), discarded));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(discarded.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Carnage, Crimson Chaos");
        harness.assertNotOnBattlefield(player1, "Carnage, Crimson Chaos");
    }

    @Test
    @DisplayName("The ETB can return a creature with mana value exactly three")
    void returnsCreatureAtManaValueLimit() {
        BenalishKnight eligible = new BenalishKnight();
        harness.setGraveyard(player1, List.of(eligible));

        castCarnageFromHand();
        chooseReturnedCreature(eligible);

        harness.assertOnBattlefield(player1, "Benalish Knight");
        harness.assertNotInGraveyard(player1, "Benalish Knight");
    }

    @Test
    @DisplayName("The granted abilities persist after Carnage leaves the battlefield")
    void grantsPersistAfterCarnageLeaves() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        castCarnageFromHand();
        chooseReturnedCreature(eligible);
        Permanent carnage = findPermanents(player1, "Carnage, Crimson Chaos").getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, carnage));
        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        returned.setSummoningSick(false);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        gs.declareAttackers(gd, player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(returned)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Carnage, Crimson Chaos");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void castCarnageFromHand() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new CarnageCrimsonChaos()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseReturnedCreature(Card card) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
