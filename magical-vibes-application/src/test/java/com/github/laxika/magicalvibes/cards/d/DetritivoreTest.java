package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.t.TeferiMageOfZhalfir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Detritivore.class, Forest.class, DreadshipReef.class, TeferiMageOfZhalfir.class, PithingNeedle.class})
class DetritivoreTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualNonbasicLandCardsInOpponentsGraveyards() {
        Permanent detritivore = harness.addToBattlefieldAndReturn(player1, new Detritivore());
        harness.setGraveyard(player1, List.of(new DreadshipReef()));
        harness.setGraveyard(player2, List.of(
                new DreadshipReef(), new DreadshipReef(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, detritivore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, detritivore)).isEqualTo(2);
    }

    @Test
    void timeCounterTriggerDestroysTargetNonbasicLand() {
        Detritivore card = suspendCard(1);
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new DreadshipReef());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(nonbasicLand.getId());
        harness.handlePermanentChosen(player1, nonbasicLand.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Dreadship Reef");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Dreadship Reef");
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void suspendXExilesCardWithXTimeCounters() {
        Detritivore card = suspendCard(2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void timeCounterTriggerFiresBeforeTheLastCounter() {
        Detritivore card = suspendCard(2);
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new DreadshipReef());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(nonbasicLand.getId());
        harness.handlePermanentChosen(player1, nonbasicLand.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dreadship Reef");
        harness.assertInGraveyard(player2, "Dreadship Reef");
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
    }

    @Test
    void suspendXCannotBeZero() {
        Detritivore card = new Detritivore();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    private Detritivore suspendCard(int xValue) {
        Detritivore card = new Detritivore();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 3);
        harness.activateHandAbility(player1, 0, null, xValue);
        return card;
    }

    @Test
    void powerAndToughnessUpdateWhenOpponentsGraveyardChanges() {
        Permanent detritivore = harness.addToBattlefieldAndReturn(player1, new Detritivore());
        harness.setGraveyard(player2, List.of(new DreadshipReef()));
        assertThat(gqs.getEffectivePower(gd, detritivore)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, detritivore)).isEqualTo(1);

        harness.setGraveyard(player2, List.of(new DreadshipReef(), new DreadshipReef()));
        assertThat(gqs.getEffectivePower(gd, detritivore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, detritivore)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new Forest()));
        assertThat(gqs.getEffectivePower(gd, detritivore)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, detritivore)).isZero();
    }

    @Test
    void canBeCastNormallyWithoutDestroyingALand() {
        harness.setGraveyard(player2, List.of(new DreadshipReef()));
        harness.addToBattlefield(player2, new DreadshipReef());
        harness.setHand(player1, List.of(new Detritivore()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Detritivore");
        harness.assertOnBattlefield(player2, "Dreadship Reef");
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Detritivore"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Detritivore"))).isEqualTo(1);
    }

    @Test
    void timeCounterTriggerCanDestroyYourOwnNonbasicLand() {
        suspendCard(2);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DreadshipReef());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dreadship Reef");
        harness.assertInGraveyard(player1, "Dreadship Reef");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void flashAllowsSuspendingDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        Detritivore card = suspendCard(2);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void suspendIsNotBlockedByPithingNeedle() {
        Permanent needle = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        needle.setChosenName("Detritivore");

        Detritivore card = suspendCard(1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingLastCounterCreatesCastTriggerBeforeOfferingToCast() {
        Detritivore card = suspendCard(1);
        harness.setGraveyard(player2, List.of(new DreadshipReef()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void lastCounterAllowsCastingWithoutManaAndGrantsHaste() {
        Detritivore card = suspendCard(1);
        harness.setGraveyard(player2, List.of(new DreadshipReef()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Detritivore");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        Permanent detritivore = findPermanent(player1, "Detritivore");
        assertThat(gqs.hasKeyword(gd, detritivore, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, detritivore)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, detritivore)).isEqualTo(1);
    }

    @Test
    void opponentsTeferiPreventsCastingWhenLastCounterIsRemoved() {
        Detritivore card = suspendCard(1);
        harness.setGraveyard(player2, List.of(new DreadshipReef()));
        harness.addToBattlefield(player2, new TeferiMageOfZhalfir());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        harness.assertNotOnBattlefield(player1, "Detritivore");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.stack).isEmpty();
    }
}
