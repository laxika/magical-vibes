package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JadeCastSentinel.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class JadeCastSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a card from your graveyard on the bottom of your library")
    void tucksOwnGraveyardCard() {
        int sentinelIndex = addSentinel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, sentinelIndex, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Puts a card from an opponent's graveyard on the bottom of its owner's library")
    void tucksOpponentGraveyardCard() {
        int sentinelIndex = addSentinel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, sentinelIndex, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Can put a noncreature card into an empty owner's library")
    void tucksLandIntoEmptyLibrary() {
        int sentinelIndex = addSentinel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card tucked = new Forest();
        harness.setGraveyard(player2, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player2, new ArrayList<>());
        List<Card> controllerLibrary = new ArrayList<>(gd.playerDecks.get(player1.getId()));

        harness.activateAbilityWithGraveyardTargets(player1, sentinelIndex, 0, List.of(tucked.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()).get(sentinelIndex).isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(tucked);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(tucked);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(controllerLibrary);
    }

    @Test
    @DisplayName("Does not move another card when its target leaves the graveyard")
    void missingTargetDoesNotRetarget() {
        int sentinelIndex = addSentinel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card target = new JadeCastSentinel();
        Card other = new JadeCastSentinel();
        Card libraryCard = new JadeCastSentinel();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target, other)));
        harness.setLibrary(player2, new ArrayList<>(List.of(libraryCard)));

        harness.activateAbilityWithGraveyardTargets(player1, sentinelIndex, 0, List.of(target.getId()));
        harness.setGraveyard(player2, new ArrayList<>(List.of(other)));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot activate with only one mana")
    void requiresTwoMana() {
        int sentinelIndex = addSentinel();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Card target = new JadeCastSentinel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, sentinelIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void requiresUntappedSource() {
        int sentinelIndex = addSentinel();
        gd.playerBattlefields.get(player1.getId()).get(sentinelIndex).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card target = new JadeCastSentinel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, sentinelIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void requiresSourceWithoutSummoningSickness() {
        int sentinelIndex = addSentinel();
        gd.playerBattlefields.get(player1.getId()).get(sentinelIndex).setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card target = new JadeCastSentinel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, sentinelIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    private int addSentinel() {
        Permanent sentinel = addCreatureReady(player1, new JadeCastSentinel());
        return gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
    }
}
