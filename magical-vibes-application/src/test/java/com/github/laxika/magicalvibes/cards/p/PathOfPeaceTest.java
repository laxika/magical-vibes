package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VolunteerMilitia;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzlyBears.class, PathOfPeace.class, Plains.class, VolunteerMilitia.class})
class PathOfPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Path of Peace puts it on the stack with the creature target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new VolunteerMilitia());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Volunteer Militia");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys the target creature and its owner gains 4 life")
    void destroysCreatureAndOwnerGainsLife() {
        harness.addToBattlefield(player2, new VolunteerMilitia());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int casterLifeBefore = harness.getGameData().getLife(player1.getId());
        int ownerLifeBefore = harness.getGameData().getLife(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Volunteer Militia");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Volunteer Militia");
        harness.assertInGraveyard(player2, "Volunteer Militia");
        harness.assertLife(player2, ownerLifeBefore + 4);
        harness.assertLife(player1, casterLifeBefore);
    }

    @Test
    @DisplayName("Gives 4 life to the target's owner when another player controls it")
    void givesLifeToOwnerWhenAnotherPlayerControlsTarget() {
        VolunteerMilitia targetCard = new VolunteerMilitia();
        targetCard.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, targetCard);
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = harness.getGameData().getLife(player2.getId());
        int controllerLifeBefore = harness.getGameData().getLife(player1.getId());
        UUID targetId = harness.getPermanentId(player1, "Volunteer Militia");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Volunteer Militia");
        harness.assertInGraveyard(player2, "Volunteer Militia");
        harness.assertLife(player2, ownerLifeBefore + 4);
        harness.assertLife(player1, controllerLifeBefore);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles with no life gain when the target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new VolunteerMilitia());
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = harness.getGameData().getLife(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Volunteer Militia");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertLife(player2, ownerLifeBefore);
    }

    @Test
    @DisplayName("Its owner gains life even when the target creature is indestructible")
    void ownerGainsLifeWhenCreatureIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new PathOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = gd.getLife(player2.getId());
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, ownerLifeBefore + 4);
    }
}
