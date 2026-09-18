package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DroolingOgre;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpireGolem.class, Island.class, DarksteelCitadel.class, DroolingOgre.class})
class SpireGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Islands reduces the generic mana cost")
    void affinityForIslandsReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Island());
        }
        harness.setHand(player1, List.of(new SpireGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only Islands controlled by the spell's controller")
    void affinityCountsOnlyControlledIslands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        harness.setHand(player1, List.of(new SpireGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity counts tapped Islands")
    void affinityCountsTappedIslands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        }
        harness.setHand(player1, List.of(new SpireGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts Islands rather than other lands")
    void affinityCountsIslandsRatherThanOtherLands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new DarksteelCitadel());
        }
        harness.setHand(player1, List.of(new SpireGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flying Spire Golem cannot be blocked by a creature without flying")
    void flyingCannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new SpireGolem());
        addCreatureReady(player2, new DroolingOgre());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
