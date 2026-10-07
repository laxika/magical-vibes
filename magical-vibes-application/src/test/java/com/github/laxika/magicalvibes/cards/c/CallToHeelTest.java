package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallToHeel.class, DregscapeZombie.class, Island.class, ObeliskOfBant.class})
class CallToHeelTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature to owner's hand and its controller draws a card")
    void resolvingBouncesAndControllerDraws() {
        harness.addToBattlefield(player2, new DregscapeZombie());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new CallToHeel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handBefore = harness.getGameData().playerHands.get(player2.getId()).size();
        UUID targetId = harness.getPermanentId(player2, "Dregscape Zombie");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Dregscape Zombie");
        // Bounced creature + drawn Island both in the controller's hand (started empty)
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        harness.assertInHand(player2, "Dregscape Zombie");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Bouncing own creature lets the caster draw a card")
    void bouncingOwnCreatureDrawsForCaster() {
        harness.addToBattlefield(player1, new DregscapeZombie());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new CallToHeel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Dregscape Zombie");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInHand(player1, "Dregscape Zombie");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new DregscapeZombie()); // valid target so spell is playable
        harness.addToBattlefield(player2, new ObeliskOfBant());
        harness.setHand(player1, List.of(new CallToHeel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Obelisk of Bant");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A creature returns to its owner while its different controller draws")
    void differentOwnerAndController() {
        DregscapeZombie creature = new DregscapeZombie();
        creature.setOwnerId(player1.getId());
        var permanent = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new CallToHeel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, permanent.getId());

        harness.assertNotOnBattlefield(player2, "Dregscape Zombie");
        harness.assertInHand(player1, "Dregscape Zombie");
        harness.assertNotInHand(player1, "Island");
        harness.assertNotInHand(player2, "Dregscape Zombie");
        harness.assertInHand(player2, "Island");
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal target prevents the draw as well as the return")
    void targetLeavesBeforeResolution() {
        var creature = harness.addToBattlefieldAndReturn(player2, new DregscapeZombie());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new CallToHeel(), new CallToHeel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Dregscape Zombie");
        assertThat(harness.getGameData().playerDecks.get(player2.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(harness.getGameData().playerDecks.get(player2.getId())).hasSize(1);
        assertThat(harness.getGameData().playerHands.get(player2.getId())).hasSize(2);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
