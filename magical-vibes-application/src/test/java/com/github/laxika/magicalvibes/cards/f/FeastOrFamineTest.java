package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeastOrFamineTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 1 creates a 2/2 black Zombie token")
    void createsZombieToken() {
        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getEffectivePower()).isEqualTo(2);
        assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Feast or Famine");
    }

    @Test
    @DisplayName("Mode 2 destroys a nonartifact, nonblack creature")
    void destroysCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 2 cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent skeletons = findPermanent(player2, "Drudge Skeletons");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, skeletons.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent ornithopter = findPermanent(player2, "Ornithopter");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
