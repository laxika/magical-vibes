package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GalestrikeTest extends BaseCardTest {

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.tap();
        gd.playerBattlefields.get(owner.getId()).add(creature);
        return creature;
    }

    private void castGalestrike(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Galestrike()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns target tapped creature to owner's hand and caster draws a card")
    void bouncesTappedCreatureAndCasterDraws() {
        Permanent creature = addTappedCreature(player2);
        harness.setLibrary(player1, List.of(new Island()));

        castGalestrike(creature.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        // Tapped creature so the spell is playable
        addTappedCreature(player2);

        Permanent untapped = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(untapped);

        harness.setHand(player1, List.of(new Galestrike()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untapped.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
