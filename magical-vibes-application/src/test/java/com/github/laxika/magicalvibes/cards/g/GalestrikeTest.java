package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Galestrike.class, DuneBeetle.class, Island.class})
class GalestrikeTest extends BaseCardTest {

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent creature = harness.addToBattlefieldAndReturn(owner, new DuneBeetle());
        creature.tap();
        return creature;
    }

    private void castGalestrike(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Galestrike()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Returns target tapped creature to owner's hand and caster draws a card")
    void bouncesTappedCreatureAndCasterDraws() {
        Permanent creature = addTappedCreature(player2);
        harness.setLibrary(player1, List.of(new Island()));

        castGalestrike(creature.getId());

        harness.assertNotOnBattlefield(player2, "Dune Beetle");
        harness.assertInHand(player2, "Dune Beetle");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        // Tapped creature so the spell is playable
        addTappedCreature(player2);

        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());

        harness.setHand(player1, List.of(new Galestrike()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untapped.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetTappedNoncreature() {
        addTappedCreature(player2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        land.tap();
        harness.setHand(player1, List.of(new Galestrike()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canReturnOwnTappedCreature() {
        Permanent creature = addTappedCreature(player1);
        harness.setLibrary(player1, List.of(new Island()));

        castGalestrike(creature.getId());

        harness.assertNotOnBattlefield(player1, "Dune Beetle");
        harness.assertInHand(player1, "Dune Beetle");
        harness.assertInHand(player1, "Island");
    }

    @Test
    void returnsStolenCreatureToOwnerRatherThanController() {
        Permanent creature = addTappedCreature(player2);
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Island()));

        castGalestrike(creature.getId());

        harness.assertNotOnBattlefield(player2, "Dune Beetle");
        harness.assertInHand(player1, "Dune Beetle");
        harness.assertNotInHand(player2, "Dune Beetle");
        harness.assertInHand(player1, "Island");
    }

    @Test
    void untappedTargetPreventsBothReturnAndDraw() {
        Permanent creature = addTappedCreature(player2);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Galestrike()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, creature.getId());
        creature.untap();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dune Beetle");
        harness.assertNotInHand(player2, "Dune Beetle");
        harness.assertNotInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Galestrike");
    }

    @Test
    void removedTargetPreventsDraw() {
        Permanent creature = addTappedCreature(player2);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Galestrike()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, creature.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, creature);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Dune Beetle");
        harness.assertNotInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Galestrike");
    }
}
