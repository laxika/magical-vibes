package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScreechingSliver.class, SidewinderSliver.class, BenalishCavalry.class})
class ScreechingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Screeching Sliver grants itself the mill ability")
    void grantsAbilityToItself() {
        Permanent screechingSliver = addCreatureReady(player1, new ScreechingSliver());
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
        assertThat(screechingSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Screeching Sliver may target its controller")
    void millsItsController() {
        Permanent screechingSliver = addCreatureReady(player1, new ScreechingSliver());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(screechingSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Sliver gains the mill ability")
    void grantsAbilityToAnotherSliver() {
        addCreatureReady(player1, new ScreechingSliver());
        Permanent otherSliver = addCreatureReady(player1, new SidewinderSliver());
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
        assertThat(otherSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Sliver also gains the mill ability")
    void grantsAbilityToOpposingSliver() {
        addCreatureReady(player1, new ScreechingSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SidewinderSliver());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(opposingSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the mill ability")
    void doesNotGrantAbilityToNonSliver() {
        addCreatureReady(player1, new ScreechingSliver());
        addCreatureReady(player1, new BenalishCavalry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("The ability mills exactly the top card into the target player's graveyard")
    void millsExactlyTheTopCard() {
        addCreatureReady(player1, new ScreechingSliver());
        SidewinderSliver topCard = new SidewinderSliver();
        BenalishCavalry nextCard = new BenalishCavalry();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An empty library is a legal target and milling it does not cause a loss")
    void canMillAnEmptyLibrary() {
        Permanent sliver = addCreatureReady(player1, new ScreechingSliver());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
        assertThat(sliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Sliver cannot pay the granted tap cost")
    void summoningSicknessPreventsActivation() {
        addCreatureReady(player1, new ScreechingSliver());
        Permanent sliver = addCreatureReady(player1, new SidewinderSliver());
        sliver.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(sliver.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Sliver cannot pay the granted tap cost again")
    void tappedSliverCannotActivate() {
        addCreatureReady(player1, new ScreechingSliver());
        Permanent sliver = addCreatureReady(player1, new SidewinderSliver());
        sliver.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Screeching Sliver removes the grant but does not counter an activated ability")
    void pendingAbilityResolvesAfterGrantSourceLeaves() {
        Permanent source = addCreatureReady(player1, new ScreechingSliver());
        Permanent sliver = addCreatureReady(player1, new SidewinderSliver());
        BenalishCavalry topCard = new BenalishCavalry();
        harness.setLibrary(player2, List.of(topCard));
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 1, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        sliver.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
