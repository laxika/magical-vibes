package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RainOfTears.class, Mountain.class, RishadanPort.class, LowlandGiant.class})
class RainOfTearsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rain of Tears puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvingDestroysTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Can destroy own land")
    void canDestroyOwnLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Mountain");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Rain of Tears goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rain of Tears");
    }

    @Test
    @DisplayName("Can destroy a nonbasic land")
    void canDestroyNonbasicLand() {
        harness.addToBattlefield(player2, new RishadanPort());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Rishadan Port");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Rishadan Port");
        harness.assertInGraveyard(player2, "Rishadan Port");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Rain of Tears");
    }

    @Test
    @DisplayName("Fizzles if target stops being a land before resolution")
    void fizzlesIfTargetStopsBeingLand() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, mountain.getId());

        var targetCard = TestCards.mutableCard(mountain);
        targetCard.setType(CardType.CREATURE);
        targetCard.setPower(1);
        targetCard.setToughness(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Rain of Tears");
    }

    @Test
    @DisplayName("Cannot destroy a creature with Rain of Tears")
    void cannotDestroyCreature() {
        harness.addToBattlefield(player2, new LowlandGiant());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID creatureId = harness.getPermanentId(player2, "Lowland Giant");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
