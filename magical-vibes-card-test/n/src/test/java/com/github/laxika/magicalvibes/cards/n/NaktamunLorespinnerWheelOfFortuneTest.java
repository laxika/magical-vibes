package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WheelOfFortune;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaktamunLorespinnerWheelOfFortune.class, WheelOfFortune.class, GrizzlyBears.class})
class NaktamunLorespinnerWheelOfFortuneTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared at your upkeep when any player has one or fewer cards in hand")
    void becomesPreparedWhenAnyPlayerHasSmallHand() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lorespinner.isPrepared()).isTrue();
        assertThat(lorespinner.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared when every player has more than one card in hand")
    void doesNotBecomePreparedWhenAllPlayersHaveLargeHands() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(lorespinner.isPrepared()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not become prepared if the hand condition is false when the trigger resolves")
    void rechecksHandConditionAtResolution() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(lorespinner.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("Casting the prepared Wheel of Fortune copy discards hands, draws seven, and unprepares the creature")
    void castingPreparedWheelOfFortuneResolvesAndUnprepares() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        fillLibraries(7);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        UUID copyId = lorespinner.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(lorespinner.isPrepared()).isFalse();
        assertThat(lorespinner.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    @DisplayName("An empty controller hand satisfies the upkeep condition")
    void becomesPreparedWithEmptyControllerHand() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lorespinner.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(lorespinner.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("An opponent's upkeep does not prepare the creature")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(lorespinner.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("A different player may satisfy the hand condition when the trigger resolves")
    void qualifyingPlayerCanChangeBeforeResolution() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(lorespinner.isPrepared()).isTrue();
    }

    @Test
    @DisplayName("An already prepared creature does not create a second spell copy")
    void repeatedPreparationKeepsExistingCopy() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        UUID copyId = lorespinner.getPreparedSpellCardId();
        int exileCount = gd.exiledCards.size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lorespinner.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.exiledCards.size()).isEqualTo(exileCount);
    }

    @Test
    @DisplayName("The prepared spell still requires its mana cost")
    void insufficientManaDoesNotUnprepareCreature() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        UUID copyId = lorespinner.getPreparedSpellCardId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lorespinner.isPrepared()).isTrue();
        assertThat(lorespinner.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting unprepares immediately and the resolved spell copy does not enter the graveyard")
    void castingUnpreparesBeforeResolutionAndCopyCeasesToExist() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of());
        fillLibraries(7);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        UUID copyId = lorespinner.getPreparedSpellCardId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, copyId);

        assertThat(gd.stack).hasSize(1);
        assertThat(lorespinner.isPrepared()).isFalse();
        assertThat(lorespinner.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    @DisplayName("The prepared Wheel of Fortune cannot be cast during upkeep")
    void preparedSpellStillRequiresSorceryTiming() {
        Permanent lorespinner = harness.addToBattlefieldAndReturn(player1,
                new NaktamunLorespinnerWheelOfFortune());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        UUID copyId = lorespinner.getPreparedSpellCardId();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");

        assertThat(lorespinner.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private void fillLibraries(int cardsEach) {
        harness.setLibrary(player1, IntStream.range(0, cardsEach)
                .mapToObj(i -> new GrizzlyBears()).toList());
        harness.setLibrary(player2, IntStream.range(0, cardsEach)
                .mapToObj(i -> new GrizzlyBears()).toList());
    }
}
