package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.BLUE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BottomlessPoolLockerRoom.class)
class BottomlessPoolLockerRoomTest extends BaseCardTest {

    @Test
    void bottomlessPoolReturnsTheChosenCreatureToItsOwnersHand() {
        Card creatureCard = creature("Target creature");
        Permanent creature = addCreatureReady(player2, creatureCard);

        castRoom(0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player2.getId())).contains(creatureCard);
    }

    @Test
    void bottomlessPoolCanBeResolvedWithoutChoosingACreature() {
        Permanent creature = addCreatureReady(player2, creature("Creature"));

        castRoom(0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void lockerRoomDrawsOnlyOnceForMultipleCreaturesDealingCombatDamage() {
        castRoom(1);
        Permanent firstAttacker = addCreatureReady(player1, creature("First attacker"));
        Permanent secondAttacker = addCreatureReady(player1, creature("Second attacker"));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new BottomlessPoolLockerRoom()));
        harness.addMana(player1, BLUE, doorIndex == 0 ? 1 : 5);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst()
                .orElseThrow();
    }

    private Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
