package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PathOfPeace.class, GrizzlyBears.class, Plains.class})
class PathOfPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Path of Peace puts it on the stack with the creature target")
    void castingPutsOnStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving destroys the target creature and its owner gains 4 life")
    void destroysCreatureAndOwnerGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int casterLifeBefore = harness.getGameData().getLife(player1.getId());
        int ownerLifeBefore = harness.getGameData().getLife(player2.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // The creature's owner (player2) gains 4 life, not the caster.
        harness.assertLife(player2, ownerLifeBefore + 4);
        harness.assertLife(player1, casterLifeBefore);
    }

    @Test
    @DisplayName("Its owner gains life when the destroyed creature is controlled by another player")
    void ownerGainsLifeWhenCreatureIsControlledByAnotherPlayer() {
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = gd.getLife(player1.getId());
        int controllerLifeBefore = gd.getLife(player2.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, ownerLifeBefore + 4);
        harness.assertLife(player2, controllerLifeBefore);
    }

    @Test
    @DisplayName("Its owner gains life even when the target creature is indestructible")
    void ownerGainsLifeWhenCreatureIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = gd.getLife(player2.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, ownerLifeBefore + 4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles with no life gain when the target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = harness.getGameData().getLife(player2.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player2, ownerLifeBefore);
    }
}
