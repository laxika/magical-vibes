package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TezzeretTheSeekerTest extends BaseCardTest {

    // ===== +1: Untap up to two target artifacts =====

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

    // ===== -X: Search library for an artifact with mana value X or less =====

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

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mind Stone"));
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

    // ===== -5: Artifacts you control become 5/5 artifact creatures =====

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

    // ===== Helpers =====

    private Permanent addReadyTezzeret(Player player, int loyalty) {
        TezzeretTheSeeker card = new TezzeretTheSeeker();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
