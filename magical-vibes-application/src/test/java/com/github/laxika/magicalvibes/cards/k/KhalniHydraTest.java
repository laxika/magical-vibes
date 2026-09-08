package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LodestoneGolem;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KhalniHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Khalni Hydra costs one less green mana for each green creature you control")
    void costsOneLessPerGreenCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KhalniHydra()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-green creatures do not reduce Khalni Hydra's cost")
    void nonGreenCreaturesDoNotReduceCost() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new KhalniHydra()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Green creatures controlled by an opponent do not reduce Khalni Hydra's cost")
    void opponentGreenCreaturesDoNotReduceCost() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KhalniHydra()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Khalni Hydra's reduction also covers an extra generic cost after its green pips")
    void reductionCoversGenericCostAfterGreenPips() {
        harness.addToBattlefield(player1, new LodestoneGolem());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new KhalniHydra()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
