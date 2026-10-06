package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkullsnapNuisance.class, GloriousAnthem.class, DazzlingTheaterPropRoom.class, Forest.class})
class SkullsnapNuisanceTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control triggers surveil 1")
    void enchantmentEntryTriggersSurveil() {
        harness.addToBattlefield(player1, new SkullsnapNuisance());
        Card topCard = new Forest();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Fully unlocking a Room triggers surveil 1")
    void fullyUnlockingRoomTriggersSurveil() {
        Permanent room = castRoom();
        harness.addToBattlefield(player1, new SkullsnapNuisance());
        Card topCard = new Forest();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    private Permanent castRoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("A Room entering triggers once, and surveil may leave the card on top")
    void roomEntryCanKeepTopCard() {
        harness.addToBattlefield(player1, new SkullsnapNuisance());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of());

        castRoom();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveilling an empty library finishes without a choice")
    void emptyLibrarySurveilFinishes() {
        harness.addToBattlefield(player1, new SkullsnapNuisance());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());

        castRoom();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A non-enchantment entering does not trigger surveil")
    void creatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkullsnapNuisance());
        harness.castFromHand(player1, new SkullsnapNuisance(), "{U}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's enchantment does not trigger surveil")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkullsnapNuisance());
        gd.activePlayerId = player2.getId();
        harness.setHand(player2, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.castModalSorcery(player2, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
