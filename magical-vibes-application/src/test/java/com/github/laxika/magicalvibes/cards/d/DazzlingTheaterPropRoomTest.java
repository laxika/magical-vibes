package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CultHealer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DazzlingTheaterPropRoom.class, CultHealer.class})
class DazzlingTheaterPropRoomTest extends BaseCardTest {

    @Test
    void castingDazzlingTheaterUnlocksItsDoorAndGrantsConvokeToCreatureSpells() {
        Permanent room = castRoom(0);
        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();

        Permanent convoker = harness.addToBattlefieldAndReturn(player1, simpleCreature("Convoker"));
        convoker.setSummoningSick(false);
        Card creatureSpell = simpleCreature("Creature spell");
        harness.setHand(player1, List.of(creatureSpell));
        harness.addMana(player1, WHITE, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Creature spell"));
    }

    @Test
    void castingPropRoomUnlocksItsDoorAndUntapsCreaturesDuringOpponentsUntapStep() {
        Permanent room = castRoom(1);
        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
        assertThat(room.isRoomDoorUnlocked(0)).isFalse();

        Permanent creature = harness.addToBattlefieldAndReturn(player1, simpleCreature("Creature"));
        creature.setSummoningSick(false);
        creature.tap();

        harness.forceStep(TurnStep.UNTAP);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void fullyUnlockingRoomTriggersAlliedRoomAbilities() {
        Permanent room = castRoom(0);
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        harness.addMana(player1, WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, healer,
                com.github.laxika.magicalvibes.model.Keyword.LIFELINK)).isTrue();
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, WHITE, doorIndex == 0 ? 4 : 3);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
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
