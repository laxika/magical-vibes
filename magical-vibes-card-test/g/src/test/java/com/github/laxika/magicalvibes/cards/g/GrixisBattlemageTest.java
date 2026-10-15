package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrixisBattlemage.class, GrizzlyBears.class, FountainOfYouth.class, Island.class})
class GrixisBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Blue ability draws a card then discards a card")
    void blueAbilityLoots() {
        addCreatureReady(player1, new GrixisBattlemage());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Drew the Island (hand = 2), now must discard one.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Red ability makes target creature unable to block this turn")
    void redAbilityMakesTargetUnableToBlock() {
        addCreatureReady(player1, new GrixisBattlemage());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Red ability cannot target a noncreature permanent")
    void redAbilityCannotTargetNoncreature() {
        addCreatureReady(player1, new GrixisBattlemage());
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate blue ability without blue mana")
    void cannotLootWithoutMana() {
        addCreatureReady(player1, new GrixisBattlemage());
        harness.setLibrary(player1, List.of(new Island()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canLootWithEmptyHandAndDiscardTheDrawnCard() {
        addCreatureReady(player1, new GrixisBattlemage());
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateEitherAbilityWhileTapped() {
        Permanent source = addCreatureReady(player1, new GrixisBattlemage());
        source.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateEitherAbilityWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrixisBattlemage());
        source.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItselfAndRestrictionExpiresAtCleanup() {
        Permanent source = addCreatureReady(player1, new GrixisBattlemage());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, source.getId());
        assertThat(source.isTapped()).isTrue();
        assertThat(source.isCantBlockThisTurn()).isFalse();
        harness.passBothPriorities();
        assertThat(source.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(source.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void affectedCreatureCannotBlockUntilCleanup() {
        addCreatureReady(player1, new GrixisBattlemage());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        assertThat(bls.canBlock(gd, target)).isTrue();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlock(gd, target)).isFalse();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    void redAbilityRequiresRedMana() {
        Permanent source = addCreatureReady(player1, new GrixisBattlemage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
    @Test
    void lootResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new GrixisBattlemage());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(source.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.stack).isEmpty();
    }
}
