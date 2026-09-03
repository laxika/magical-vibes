package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbermageGoblin.class, GrizzlyBears.class, LlanowarElves.class})
class EmbermageGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may search prompt")
    void enteringTheBattlefieldCreatesMaySearchPrompt() {
        setupAndCast();

        resolveCreatureAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may ability searches for another copy")
    void acceptingMaySearchesForAnotherCopy() {
        setupAndCast();
        EmbermageGoblin copy = new EmbermageGoblin();
        GrizzlyBears filler = new GrizzlyBears();
        setLibrary(copy, filler);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(copy);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(copy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningMayDoesNotSearch() {
        setupAndCast();
        EmbermageGoblin copy = new EmbermageGoblin();
        setLibrary(copy);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(copy);
    }

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyGoblin(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void deals1DamageToCreature() {
        addReadyGoblin(player1);
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent goblin = new Permanent(new EmbermageGoblin());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(goblin);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent goblin = addReadyGoblin(player1);
        goblin.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new EmbermageGoblin()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyGoblin(Player player) {
        Permanent perm = new Permanent(new EmbermageGoblin());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
