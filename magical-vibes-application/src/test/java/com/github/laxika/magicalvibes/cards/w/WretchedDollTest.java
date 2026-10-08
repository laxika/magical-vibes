package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WretchedDoll.class})
class WretchedDollTest extends BaseCardTest {

    @Test
    @DisplayName("Surveil 1 can put the top card into the graveyard")
    void surveilAccepted() {
        Permanent doll = addReadyDoll();
        Card topCard = new WretchedDoll();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(doll.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveil 1 can leave the top card on the library")
    void surveilDeclined() {
        addReadyDoll();
        Card topCard = new WretchedDoll();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Wretched Doll cannot activate without black mana")
    void requiresBlackMana() {
        addReadyDoll();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped Wretched Doll cannot activate")
    void requiresUntappedDoll() {
        Permanent doll = addReadyDoll();
        doll.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Wretched Doll cannot activate")
    void requiresNoSummoningSickness() {
        Permanent doll = addReadyDoll();
        doll.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(doll.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil resolves with an empty library without requesting a choice")
    void emptyLibrary() {
        Permanent doll = addReadyDoll();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(doll.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The activated ability still surveils after Wretched Doll leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent doll = addReadyDoll();
        Card topCard = new WretchedDoll();
        Card nextCard = new WretchedDoll();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(doll.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        gd.playerBattlefields.get(player1.getId()).remove(doll);
        gd.playerGraveyards.get(player1.getId()).add(doll.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard, doll.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyDoll() {
        return addCreatureReady(player1, new WretchedDoll());
    }
}
