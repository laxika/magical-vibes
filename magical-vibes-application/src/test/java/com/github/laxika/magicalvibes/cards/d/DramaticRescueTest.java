package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.e.EtherealArmor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DramaticRescue.class, AxebaneStag.class, EtherealArmor.class})
class DramaticRescueTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature to owner's hand and caster gains 2 life")
    void returnsCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new AxebaneStag());
        harness.setHand(player1, List.of(new DramaticRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Axebane Stag");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Axebane Stag");
        harness.assertInHand(player2, "Axebane Stag");
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Dramatic Rescue");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, new AxebaneStag()).getId();
        harness.addToBattlefieldAndReturn(player2, new EtherealArmor()).setAttachedTo(creatureId);
        harness.setHand(player1, List.of(new DramaticRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Ethereal Armor");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles with no life gain if the target leaves the battlefield")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new AxebaneStag());
        harness.setHand(player1, List.of(new DramaticRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Axebane Stag");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Dramatic Rescue");
    }
    @Test
    @DisplayName("Can return your own creature and still gain life")
    void returnsOwnCreatureAndGainsLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new AxebaneStag()).getId();
        harness.setHand(player1, List.of(new DramaticRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Axebane Stag");
        harness.assertInHand(player1, "Axebane Stag");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Dramatic Rescue");
    }

    @Test
    @DisplayName("Returns a creature to its owner rather than its current controller")
    void returnsCreatureToOwnerRatherThanController() {
        AxebaneStag creature = new AxebaneStag();
        creature.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, creature).getId();
        harness.setHand(player1, List.of(new DramaticRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Axebane Stag");
        harness.assertInHand(player1, "Axebane Stag");
        harness.assertNotInHand(player2, "Axebane Stag");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
