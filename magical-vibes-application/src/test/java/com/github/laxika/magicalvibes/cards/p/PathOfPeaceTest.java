package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PathOfPeace.class, CoralMerfolk.class, Forest.class, Confiscate.class})
class PathOfPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Path of Peace puts it on the stack with the creature target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Coral Merfolk");
        harness.castSorcery(player1, 0, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys the target creature and its owner gains 4 life")
    void destroysCreatureAndOwnerGainsLife() {
        harness.addToBattlefield(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int casterLifeBefore = harness.getGameData().getLife(player1.getId());
        int ownerLifeBefore = harness.getGameData().getLife(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Coral Merfolk");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
        harness.assertInGraveyard(player2, "Coral Merfolk");
        // The creature's owner (player2) gains 4 life, not the caster.
        assertThat(gd.getLife(player2.getId())).isEqualTo(ownerLifeBefore + 4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(casterLifeBefore);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles with no life gain when the target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = harness.getGameData().getLife(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Coral Merfolk");
        harness.castSorcery(player1, 0, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.getLife(player2.getId())).isEqualTo(ownerLifeBefore);
    }

    @Test
    @DisplayName("The owner gains life when the target creature is controlled by another player")
    void ownerGainsLifeForStolenCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));

        int ownerLifeBefore = gd.getLife(player1.getId());
        int controllerLifeBefore = gd.getLife(player2.getId());
        harness.setHand(player2, List.of(new PathOfPeace()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player2, 0, 0, target.getId());

        harness.assertInGraveyard(player1, "Coral Merfolk");
        assertThat(gd.getLife(player1.getId())).isEqualTo(ownerLifeBefore + 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(controllerLifeBefore);
    }
}
