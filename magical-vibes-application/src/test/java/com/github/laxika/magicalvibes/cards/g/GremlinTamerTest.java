package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GremlinTamer.class, DazzlingTheaterPropRoom.class})
class GremlinTamerTest extends BaseCardTest {

    @Test
    void enchantmentYouControlEnteringCreatesAGremlin() {
        addTamer();
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findGremlinTokens(player1)).hasSize(1);
        assertThat(findGremlinTokens(player1).getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(findGremlinTokens(player1).getFirst().getCard().getToughness()).isEqualTo(1);
    }

    @Test
    void fullyUnlockingARoomCreatesAGremlin() {
        Permanent room = castRoom();
        addTamer();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(findGremlinTokens(player1)).hasSize(1);
    }

    @Test
    void opponentEnchantmentsDoNotCreateAGremlin() {
        addTamer();
        int tokenCountBefore = findGremlinTokens(player1).size();
        harness.setHand(player2, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.castModalSorcery(player2, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(findGremlinTokens(player1)).hasSize(tokenCountBefore);
    }

    @Test
    void roomEntryAndLaterFullUnlockEachCreateAGremlin() {
        addTamer();
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findGremlinTokens(player1)).hasSize(1);
        Permanent room = findPermanent(player1, "Dazzling Theater // Prop Room");
        assertThat(room.isRoomFullyUnlocked()).isFalse();

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(findGremlinTokens(player1)).hasSize(2);
    }

    @Test
    void opponentFullyUnlockingARoomDoesNotCreateAGremlin() {
        addTamer();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player2, ManaColor.WHITE, 7);

        harness.castModalSorcery(player2, 0, 0, List.of());
        harness.passBothPriorities();
        Permanent room = findPermanent(player2, "Dazzling Theater // Prop Room");
        harness.unlockRoomDoor(player2, 0, 1);
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(findGremlinTokens(player1)).isEmpty();
        assertThat(findGremlinTokens(player2)).isEmpty();
    }

    @Test
    void eachTamerCreatesItsOwnTokenForAnEnchantmentEntering() {
        addTamer();
        addTamer();

        harness.enterBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findGremlinTokens(player1)).hasSize(2);
        assertThat(findGremlinTokens(player2)).isEmpty();
    }

    private void addTamer() {
        harness.addToBattlefield(player1, new GremlinTamer());
    }

    private Permanent castRoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        return findPermanent(player1, "Dazzling Theater // Prop Room");
    }

    private List<Permanent> findGremlinTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.GREMLIN))
                .toList();
    }
}
