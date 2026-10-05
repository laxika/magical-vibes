package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HobgoblinCaptain;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShockingGrasp;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarlachRagingTiefling.class, Plains.class, Island.class, Swamp.class,
        Mountain.class, Forest.class, HobgoblinCaptain.class, ShockingGrasp.class})
class KarlachRagingTieflingTest extends BaseCardTest {

    @Test
    void whiteSpecializationCreatesAKnightAndBoostsYourCreatures() {
        Permanent karlach = addCreatureReady(player1, new KarlachRagingTiefling());
        Permanent support = addCreatureReady(player1, new HobgoblinCaptain());
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        payDiscardIfRequested();
        resolveAllTriggers();

        assertThat(karlach.getCard().getName()).isEqualTo("Karlach, Tiefling Zealot");
        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, karlach)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, support)).isEqualTo(4);
        Permanent knight = findPermanent(player1, "Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.HASTE)).isTrue();
    }

    @Test
    void graveyardSpecializationReturnsKarlachToTheBattlefield() {
        harness.setGraveyard(player1, List.of(new KarlachRagingTiefling()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        payDiscardIfRequested();
        resolveAllTriggers();

        Permanent karlach = findPermanent(player1, "Karlach, Tiefling Zealot");
        assertThat(karlach).isNotNull();
        harness.assertNotInGraveyard(player1, "Karlach, Raging Tiefling");
        assertThat(bls.canBlock(gd, karlach)).isFalse();
    }

    @Test
    void specializingRequiresACardToDiscard() {
        addCreatureReady(player1, new KarlachRagingTiefling());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whiteSpecializationDiscardsTheChosenPlains() {
        addCreatureReady(player1, new KarlachRagingTiefling());
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        payDiscardIfRequested();
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Plains");
    }

    @Test
    void blueSpecializationCannotBePaidByDiscardingAPlains() {
        addCreatureReady(player1, new KarlachRagingTiefling());
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    void battlefieldSpecializationDoesNotPreventKarlachFromBlocking() {
        Permanent karlach = specialize(0, new Plains());
        resolveAllTriggers();

        assertThat(bls.canBlock(gd, karlach)).isTrue();
    }

    @Test
    void graveyardReturnWaitsForItsTriggeredAbilityToResolve() {
        harness.setGraveyard(player1, List.of(new KarlachRagingTiefling()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        payDiscardIfRequested();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Karlach, Tiefling Zealot");
        harness.assertInGraveyard(player1, "Karlach, Tiefling Zealot");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Karlach, Tiefling Zealot");
    }

    @Test
    void redSpecializationStopsAnOpposingCreatureFromBlocking() {
        Permanent opponent = addCreatureReady(player2, new HobgoblinCaptain());
        specialize(3, new Mountain());
        harness.handlePermanentChosen(player1, opponent.getId());
        resolveAllTriggers();

        assertThat(bls.canBlock(gd, opponent)).isFalse();
    }

    @Test
    void greenSpecializationBoostsAnotherCreature() {
        Permanent support = addCreatureReady(player1, new HobgoblinCaptain());
        Permanent karlach = specialize(4, new Forest());
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(support.getId()).doesNotContain(karlach.getId());
        harness.handlePermanentChosen(player1, support.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, support)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, support)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, karlach)).isEqualTo(4);
    }

    @Test
    void blackSpecializationCanSacrificeACreatureToDrawAndDrain() {
        Permanent support = addCreatureReady(player1, new HobgoblinCaptain());
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.setLife(player2, 20);
        specialize(2, new Swamp());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, support.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hobgoblin Captain");
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card.getName().equals("Plains"))
                .hasSize(2);
        harness.assertLife(player2, 18);
    }

    @Test
    void blackSpecializationCanDeclineTheSacrifice() {
        harness.setLife(player2, 20);
        Permanent karlach = specialize(2, new Swamp());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(karlach);
        harness.assertLife(player2, 20);
    }

    @Test
    void blueSpecializationAllowsCastingTheSoughtCardLaterInTheTurn() {
        harness.setLibrary(player1, List.of(new ShockingGrasp(), new Plains()));
        Permanent karlach = specialize(1, new Island());
        resolveAllTriggers();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.assertInHand(player1, "Shocking Grasp");
        int index = gd.playerHands.get(player1.getId()).stream().map(Card::getName).toList()
                .indexOf("Shocking Grasp");

        harness.castAndResolveInstant(player1, index, karlach.getId());

        harness.assertInGraveyard(player1, "Shocking Grasp");
        assertThat(gqs.getEffectivePower(gd, karlach)).isEqualTo(2);
        harness.assertInHand(player1, "Plains");
    }

    private Permanent specialize(int abilityIndex, Card discard) {
        Permanent karlach = addCreatureReady(player1, new KarlachRagingTiefling());
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(karlach);
        harness.activateAbility(player1, index, abilityIndex, null, null);
        payDiscardIfRequested();
        harness.passBothPriorities();
        return karlach;
    }

    private void payDiscardIfRequested() {
        if (gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class) != null) {
            harness.handleCardChosen(player1, 0);
        }
    }
}
