package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndlessDetour.class, GrizzlyBears.class, HolyDay.class, Island.class, Shock.class})
class EndlessDetourTest extends BaseCardTest {

    @Test
    @DisplayName("The owner puts a target nonland permanent on top of their library")
    void ownerPutsNonlandPermanentOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card oldTop = new Island();
        setLibrary(player2, oldTop);
        castEndlessDetour(target.getId());

        assertOwnerChoice(player2);
        harness.handleListChoice(player2, "Put it on top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), oldTop);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The owner puts a target graveyard card on the bottom of their library")
    void ownerPutsGraveyardCardOnBottom() {
        Card target = new HolyDay();
        Card oldTop = new Island();
        harness.setGraveyard(player2, List.of(target));
        setLibrary(player2, oldTop);
        castEndlessDetour(target.getId());

        assertOwnerChoice(player2);
        harness.handleListChoice(player2, "Put it on the bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oldTop, target);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The owner puts a target spell on the bottom of their library")
    void ownerPutsSpellOnBottom() {
        Card oldTop = new Island();
        setLibrary(player2, oldTop);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new EndlessDetour()));
        addEndlessDetourMana(player1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        UUID shockId = gd.stack.getFirst().getCard().getId();

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, shockId);
        harness.passBothPriorities();

        assertOwnerChoice(player2);
        harness.handleListChoice(player2, "Put it on the bottom");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oldTop, shock);
    }

    @Test
    @DisplayName("Endless Detour cannot target a land permanent")
    void cannotTargetLandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new EndlessDetour()));
        addEndlessDetourMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEndlessDetour(UUID targetId) {
        harness.setHand(player1, List.of(new EndlessDetour()));
        addEndlessDetourMana(player1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addEndlessDetourMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }

    private void assertOwnerChoice(com.github.laxika.magicalvibes.model.Player owner) {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(owner.getId());
        assertThat(choice.options()).containsExactly("Put it on top", "Put it on the bottom");
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        gd.playerDecks.put(player.getId(), new ArrayList<>(List.of(cards)));
    }
}
