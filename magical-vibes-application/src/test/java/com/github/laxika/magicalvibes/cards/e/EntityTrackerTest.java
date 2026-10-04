package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.u.UnableToScream;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntityTracker.class, GloriousAnthem.class, DazzlingTheaterPropRoom.class, UnableToScream.class})
class EntityTrackerTest extends BaseCardTest {

    @Test
    void drawsWhenAnEnchantmentYouControlEnters() {
        harness.addToBattlefield(player1, new EntityTracker());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsWhenYouFullyUnlockARoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new EntityTracker());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantment() {
        harness.addToBattlefield(player1, new EntityTracker());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void drawsOnceWhenARoomEntersWithOnlyOneDoorUnlocked() {
        harness.addToBattlefield(player1, new EntityTracker());
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castModalSorcery(player1, 0, 0, List.of());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBeCastDuringAnOpponentsCombatAndDoesNotTriggerForItself() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new EntityTracker()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Entity Tracker");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDrawWhenAnotherNonEnchantmentCreatureEnters() {
        harness.addToBattlefield(player1, new EntityTracker());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new EntityTracker());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenAnOpponentFullyUnlocksARoom() {
        harness.addToBattlefield(player1, new EntityTracker());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new DazzlingTheaterPropRoom()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castModalSorcery(player2, 0, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player2, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerForAnEnchantmentAfterLosingAllAbilities() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new EntityTracker());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new UnableToScream()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castEnchantment(player2, 0, tracker.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerForFullyUnlockingARoomAfterLosingAllAbilities() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new EntityTracker());
        harness.setHand(player2, List.of(new UnableToScream()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castEnchantment(player2, 0, tracker.getId());
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
