package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({TezzeretTheSeeker.class, Forest.class, GildedLotus.class, GrizzlyBears.class,
        MindStone.class, RodOfRuin.class, Ornithopter.class})
class TezzeretTheSeekerTest extends BaseCardTest {


    @Test
    @DisplayName("+1 untaps two target tapped artifacts and gains loyalty")
    void plusOneUntapsTwoTargetArtifacts() {
        Permanent tezzeret = addReadyTezzeret(player1, 4);
        Permanent stone = addPermanent(player1, new MindStone());
        Permanent lotus = addPermanent(player1, new GildedLotus());
        stone.tap();
        lotus.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(stone.getId(), lotus.getId()));
        harness.passBothPriorities();

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(stone.isTapped()).isFalse();
        assertThat(lotus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+1 cannot target a non-artifact")
    void plusOneRejectsNonArtifactTarget() {
        addReadyTezzeret(player1, 4);
        Permanent stone = addPermanent(player1, new MindStone());
        Permanent creature = addPermanent(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(stone.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("-X puts an artifact with mana value X or less onto the battlefield")
    void minusXPutsArtifactOntoBattlefield() {
        Permanent tezzeret = addReadyTezzeret(player1, 5);
        setLibrary(new RodOfRuin(), new MindStone(), new Forest());

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // X=2 → only MindStone (MV 2); Rod of Ruin (MV 4) is excluded, Forest is not an artifact.
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards().stream().map(Card::getName)).containsExactly("Mind Stone");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 5 - 2
    }

    @Test
    @DisplayName("-X offers no artifact whose mana value exceeds X")
    void minusXExcludesHigherManaValue() {
        addReadyTezzeret(player1, 5);
        setLibrary(new RodOfRuin(), new GildedLotus()); // MV 4 and MV 5

        harness.activateAbility(player1, 0, 1, 1, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // No artifact with MV <= 1 → nothing to search for.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }


    @Test
    @DisplayName("-5 makes artifacts you control 5/5 creatures until end of turn")
    void minusFiveAnimatesControlledArtifacts() {
        addReadyTezzeret(player1, 5);
        Permanent stone = addPermanent(player1, new MindStone());
        Permanent bears = addPermanent(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(stone.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(stone.getAnimatedPower()).isEqualTo(5);
        assertThat(stone.getAnimatedToughness()).isEqualTo(5);
        assertThat(stone.getGrantedCardTypes()).contains(CardType.CREATURE);
        assertThat(stone.getEffectivePower()).isEqualTo(5);

        // Non-artifact creature is unaffected.
        assertThat(bears.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("-5 animation wears off at end of turn")
    void minusFiveWearsOffAtEndOfTurn() {
        addReadyTezzeret(player1, 5);
        Permanent stone = addPermanent(player1, new MindStone());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(stone.isAnimatedUntilEndOfTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stone.isAnimatedUntilEndOfTurn()).isFalse();
    }

    @Test
    @DisplayName("-5 does not affect an opponent's artifacts")
    void minusFiveDoesNotAffectOpponentArtifacts() {
        addReadyTezzeret(player1, 5);
        Permanent oppStone = addPermanent(player2, new MindStone());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(oppStone.isAnimatedUntilEndOfTurn()).isFalse();
    }

    @Test
    void plusOneCanChooseNoTargets() {
        Permanent tezzeret = addReadyTezzeret(player1, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void plusOneCanUntapOneOpponentArtifact() {
        addReadyTezzeret(player1, 4);
        Permanent stone = addPermanent(player2, new MindStone());
        stone.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(stone.getId()));
        harness.passBothPriorities();

        assertThat(stone.isTapped()).isFalse();
    }

    @Test
    void minusZeroFindsZeroManaArtifactWithoutRemovingLoyalty() {
        Permanent tezzeret = addReadyTezzeret(player1, 4);
        setLibrary(new Ornithopter(), new MindStone());

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Mind Stone");
    }

    @Test
    void minusXCanFindArtifactBelowXAfterTezzeretDiesFromLoyaltyCost() {
        addReadyTezzeret(player1, 4);
        setLibrary(new MindStone());

        harness.activateAbility(player1, 0, 1, 4, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Tezzeret the Seeker");
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void minusXCanFailToFindDespiteMatchingArtifact() {
        addReadyTezzeret(player1, 4);
        setLibrary(new MindStone());

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Mind Stone");
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusXCannotSpendMoreLoyaltyThanAvailable() {
        Permanent tezzeret = addReadyTezzeret(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 5, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void plusOneCannotChooseTheSameArtifactTwice() {
        Permanent tezzeret = addReadyTezzeret(player1, 4);
        Permanent stone = addPermanent(player1, new MindStone());
        stone.tap();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(stone.getId(), stone.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    void minusFiveAffectsExistingArtifactCreaturesButNotLaterArtifacts() {
        addReadyTezzeret(player1, 5);
        Permanent thopter = addPermanent(player1, new Ornithopter());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent laterStone = addPermanent(player1, new MindStone());

        assertThat(thopter.getEffectivePower()).isEqualTo(5);
        assertThat(thopter.getEffectiveToughness()).isEqualTo(5);
        assertThat(laterStone.isAnimatedUntilEndOfTurn()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thopter.getEffectivePower()).isZero();
        assertThat(thopter.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyTezzeret(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TezzeretTheSeeker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addPermanent(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
