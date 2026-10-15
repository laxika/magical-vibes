package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrownerOfSecrets.class, KnightOfMeadowgrain.class, MerrowCommerce.class})
class DrownerOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself as the only Merfolk and target player mills a card")
    void tapsItselfAndTargetMills() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());

        int deckBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        harness.activateAbility(player1, idx, null, player2.getId());
        harness.passBothPriorities();

        assertThat(drowner.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 1);
    }

    @Test
    @DisplayName("Can tap another Merfolk instead of itself as the cost")
    void tapsAnotherMerfolk() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        Permanent merfolk = addCreatureReady(player1, new DrownerOfSecrets());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        harness.activateAbility(player1, idx, null, player2.getId());

        // Two valid Merfolk -> choose which to tap
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, merfolk.getId());
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(drowner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate while tapped when another Merfolk pays the cost")
    void canActivateWithTappedSource() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        Permanent merfolk = addCreatureReady(player1, new DrownerOfSecrets());
        drowner.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        harness.activateAbility(player1, idx, null, player2.getId());
        harness.passBothPriorities();

        assertThat(drowner.isTapped()).isTrue();
        assertThat(merfolk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        harness.activateAbility(player1, idx, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Cannot activate with no untapped Merfolk to tap")
    void cannotActivateWithoutUntappedMerfolk() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        drowner.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use an opponent's Merfolk to pay the cost")
    void cannotTapOpponentsMerfolk() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        drowner.tap();
        addCreatureReady(player2, new DrownerOfSecrets());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    @Test
    @DisplayName("Cannot use an untapped non-Merfolk creature to pay the cost")
    void cannotTapNonMerfolk() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        drowner.tap();
        Permanent knight = addCreatureReady(player1, new KnightOfMeadowgrain());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
        assertThat(knight.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can tap a noncreature Merfolk enchantment to pay the cost")
    void canTapMerrowCommerce() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        drowner.tap();
        Permanent commerce = harness.addToBattlefieldAndReturn(player1, new MerrowCommerce());
        DrownerOfSecrets topCard = new DrownerOfSecrets();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(commerce.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
    }

    @Test
    @DisplayName("A summoning sick Drowner can tap itself and mills only the top card on resolution")
    void summoningSickSourceCanTapItself() {
        Permanent drowner = harness.addToBattlefieldAndReturn(player1, new DrownerOfSecrets());
        drowner.setSummoningSick(true);
        DrownerOfSecrets topCard = new DrownerOfSecrets();
        DrownerOfSecrets nextCard = new DrownerOfSecrets();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(drowner.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard).doesNotContain(nextCard);
    }

    @Test
    @DisplayName("Can tap another summoning sick Merfolk to pay the cost")
    void canTapAnotherSummoningSickMerfolk() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        drowner.tap();
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new DrownerOfSecrets());
        merfolk.setSummoningSick(true);
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Milling an empty library still pays the tap cost and does not make the player lose")
    void canMillEmptyLibrary() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        harness.setLibrary(player2, List.of());
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(drowner.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore);
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }
}
