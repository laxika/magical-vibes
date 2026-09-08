package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GrandEntrywayElegantRotunda.class)
class GrandEntrywayElegantRotundaTest extends BaseCardTest {

    @Test
    void unlockingGrandEntrywayCreatesAGlimmerEnchantmentCreatureToken() {
        Permanent room = castRoom(0);

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(glimmer.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(glimmer.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(glimmer.getCard().getSubtypes()).containsExactly(CardSubtype.GLIMMER);
    }

    @Test
    void unlockingElegantRotundaPutsCountersOnUpToTwoTargetCreatures() {
        Permanent room = castRoom(0);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, simpleCreature("First creature"));
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, simpleCreature("Second creature"));
        harness.addMana(player1, WHITE, 3);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new GrandEntrywayElegantRotunda()));
        harness.addMana(player1, WHITE, doorIndex == 0 ? 2 : 3);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst().orElseThrow();
    }

    private Card simpleCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{W}");
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
