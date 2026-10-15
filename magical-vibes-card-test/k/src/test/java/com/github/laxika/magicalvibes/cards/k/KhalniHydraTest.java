package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AwakeningZone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LodestoneGolem;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KhalniHydra.class, GrizzlyBears.class, LodestoneGolem.class, Memnite.class, AwakeningZone.class})
class KhalniHydraTest extends BaseCardTest {

    @Test
    @DisplayName("The Hydra being cast does not count toward its own reduction")
    void spellDoesNotCountItself() {
        harness.setHand(player1, List.of(new KhalniHydra()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Khalni Hydra can be cast for eight green mana with no creatures")
    void castsWithoutReduction() {
        harness.setHand(player1, List.of(new KhalniHydra()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Eight green creatures make Khalni Hydra free to cast")
    void eightGreenCreaturesMakeSpellFree() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new KhalniHydra());
        }
        harness.setHand(player1, List.of(new KhalniHydra()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Green noncreature permanents do not reduce Khalni Hydra's cost")
    void greenNoncreatureDoesNotReduceCost() {
        harness.addToBattlefield(player1, new AwakeningZone());
        harness.setHand(player1, List.of(new KhalniHydra()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Eight green creatures do not remove Lodestone Golem's extra generic cost")
    void eightGreenCreaturesLeaveGenericTax() {
        harness.addToBattlefield(player1, new LodestoneGolem());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new KhalniHydra());
        }
        harness.setHand(player1, List.of(new KhalniHydra()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

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
