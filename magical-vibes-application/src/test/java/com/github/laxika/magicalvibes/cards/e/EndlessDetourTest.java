package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndlessDetour.class, GrizzlyBears.class, HolyDay.class, Island.class, Shock.class, ThinkTwice.class})
class EndlessDetourTest extends BaseCardTest {

    @Test
    @DisplayName("The owner puts a target nonland permanent on top of their library")
    void ownerPutsNonlandPermanentOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card oldTop = new Island();
        harness.setLibrary(player2, List.of(oldTop));
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
        harness.setLibrary(player2, List.of(oldTop));
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
        harness.setLibrary(player2, List.of(oldTop));
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new EndlessDetour()));
        addEndlessDetourMana(player1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        UUID shockId = gd.stack.getFirst().getCard().getId();

        harness.forceActivePlayer(player1);
        harness.castAndResolveInstant(player1, 0, shockId);

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

    @Test
    void ownerPutsNonlandPermanentOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card oldTop = new Island();
        harness.setLibrary(player2, List.of(oldTop));
        castEndlessDetour(target.getId());

        assertOwnerChoice(player2);
        harness.handleListChoice(player2, "Put it on the bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oldTop, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void canPutOwnGraveyardLandOnTop() {
        Card target = new Island();
        Card oldTop = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(oldTop));
        castEndlessDetour(target.getId());

        assertOwnerChoice(player1);
        harness.handleListChoice(player1, "Put it on top");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target, oldTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Endless Detour");
    }

    @Test
    void ownerRatherThanControllerChoosesAndReceivesPermanent() {
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        Card oldTop = new Island();
        harness.setLibrary(player2, List.of(oldTop));
        castEndlessDetour(target.getId());

        assertOwnerChoice(player2);
        harness.handleListChoice(player2, "Put it on top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(card, oldTop);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void ownerPutsSpellOnTopWithoutResolvingIt() {
        Card oldTop = new Island();
        harness.setLibrary(player2, List.of(oldTop));
        HolyDay spell = new HolyDay();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{W}");
        castEndlessDetour(spell.getId());

        assertOwnerChoice(player2);
        harness.handleListChoice(player2, "Put it on top");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(spell, oldTop);
        harness.assertNotInGraveyard(player2, "Holy Day");
    }

    @Test
    void flashbackSpellIsExiledWhenOwnerChoosesTop() {
        assertFlashbackSpellIsExiled("Put it on top");
    }

    @Test
    void flashbackSpellIsExiledWhenOwnerChoosesBottom() {
        assertFlashbackSpellIsExiled("Put it on the bottom");
    }

    @Test
    void graveyardTargetThatLeavesBeforeResolutionIsNotMovedAgain() {
        Card target = new HolyDay();
        Card oldTop = new Island();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(oldTop));
        harness.setHand(player1, List.of(new EndlessDetour(), new EndlessDetour()));
        addEndlessDetourMana(player1);
        addEndlessDetourMana(player1);
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertOwnerChoice(player2);
        harness.handleListChoice(player2, "Put it on the bottom");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oldTop, target);
    }

    private void assertFlashbackSpellIsExiled(String choice) {
        ThinkTwice spell = new ThinkTwice();
        Card oldTop = new Island();
        harness.setLibrary(player2, List.of(oldTop));
        harness.setGraveyard(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.castFlashback(player2, 0);
        int handSize = gd.playerHands.get(player2.getId()).size();
        castEndlessDetour(spell.getId());

        assertOwnerChoice(player2);
        harness.handleListChoice(player2, choice);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oldTop);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        harness.assertNotInGraveyard(player2, "Think Twice");
    }

    private void castEndlessDetour(UUID targetId) {
        harness.setHand(player1, List.of(new EndlessDetour()));
        addEndlessDetourMana(player1);
        harness.castAndResolveInstant(player1, 0, targetId);
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

}
