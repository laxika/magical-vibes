package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrabblingSkullcrab.class, GloriousAnthem.class, DazzlingTheaterPropRoom.class, Forest.class})
class ScrabblingSkullcrabTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control makes a target player mill two cards")
    void enchantmentEntryMillsTargetPlayer() {
        harness.addToBattlefield(player1, new ScrabblingSkullcrab());
        setLibrary(player2, 3);
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Fully unlocking a Room makes a target player mill two cards")
    void fullyUnlockingRoomMillsTargetPlayer() {
        Permanent room = castRoom();
        harness.addToBattlefield(player1, new ScrabblingSkullcrab());
        setLibrary(player2, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A Room entering triggers once and can mill its controller with a short library")
    void roomEntryCanMillControllerWithShortLibrary() {
        harness.addToBattlefield(player1, new ScrabblingSkullcrab());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castRoom();
        Permanent room = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(room.isRoomFullyUnlocked()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger eerie")
    void opponentsEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player2, new ScrabblingSkullcrab());
        setLibrary(player1, 3);
        setLibrary(player2, 3);

        castRoom();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castRoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, int size) {
        harness.setLibrary(player, List.of(new Forest(), new Forest(), new Forest()).subList(0, size));
    }
}
