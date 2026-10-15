package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkHatchling.class, GrizzlyBears.class, ScatheZombies.class, Unsummon.class})
class DarkHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target nonblack creature")
    void etbDestroysTargetNonblackCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Dark Hatchling");
    }

    @Test
    @DisplayName("ETB can target a nonblack creature you control")
    void etbCanTargetYourOwnNonblackCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Dark Hatchling");
    }

    @Test
    @DisplayName("ETB destruction cannot be regenerated")
    void etbDestructionCannotBeRegenerated() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);

        castAndResolve(bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new ScatheZombies());
        UUID targetId = harness.getPermanentId(player2, "Scathe Zombies");
        prepareCard();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }

    @Test
    @DisplayName("ETB leaves no ability on the stack when there are no legal targets")
    void etbLeavesNoAbilityOnStackWithoutLegalTarget() {
        prepareCard();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dark Hatchling");
    }

    @Test
    @DisplayName("ETB resolves after Dark Hatchling leaves the battlefield")
    void etbResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        prepareCard();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Dark Hatchling"));
        harness.assertInHand(player1, "Dark Hatchling");
        harness.assertNotOnBattlefield(player1, "Dark Hatchling");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not destroy a replacement creature when its target leaves")
    void etbDoesNotDestroyReplacementCreatureWhenTargetLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        prepareCard();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Dark Hatchling");
        assertThat(gd.stack).isEmpty();
    }
    private void castAndResolve(UUID targetId) {
        prepareCard();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new DarkHatchling()));
        harness.addMana(player1, ManaColor.BLACK, 6);
    }
}
