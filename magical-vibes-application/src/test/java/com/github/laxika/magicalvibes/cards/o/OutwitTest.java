package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutwitTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell that targets a player")
    void countersSpellTargetingPlayer() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Outwit()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a spell that targets a creature instead of a player")
    void cannotTargetCreatureTargetingSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Outwit()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bolt.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell with no targets")
    void cannotTargetUntargetedSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Outwit()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
